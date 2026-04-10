package com.example.stepbooster

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.IBinder
import android.os.PowerManager
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Instant

class StepBoosterService : Service(), SensorEventListener {

    companion object {
        const val CHANNEL_ID = "stepbooster_channel"
        const val NOTIFICATION_ID = 1001
        const val ACTION_UPDATE_MULTIPLIER = "ACTION_UPDATE_MULTIPLIER"
        const val EXTRA_MULTIPLIER = "EXTRA_MULTIPLIER"
        const val WATCHDOG_INTERVAL_MS = 5 * 60 * 1000L // 5 min

        var realStepsToday = 0L
        var boostedStepsToday = 0L
        var currentMultiplier = 2.0f
        var isRunning = false

        fun start(context: Context) {
            val intent = Intent(context, StepBoosterService::class.java)
            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, StepBoosterService::class.java)
            context.stopService(intent)
        }
    }

    private lateinit var sensorManager: SensorManager
    private lateinit var wakeLock: PowerManager.WakeLock
    private lateinit var healthConnectManager: HealthConnectManager
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Batching
    private var batchRealSteps = 0L
    private var batchStartTime: Instant? = null
    private var batchEndTime: Instant? = null
    private var batchJob: Job? = null
    private val BATCH_INTERVAL_MS = 15_000L

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        healthConnectManager = HealthConnectManager(this)
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        // WakeLock
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "StepBooster::WakeLock")
        wakeLock.acquire()

        // Citim multiplicatorul salvat
        serviceScope.launch {
            currentMultiplier = MultiplierDataStore.getMultiplier(applicationContext).first()
        }

        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())
        registerStepSensor()
        scheduleWatchdog()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_UPDATE_MULTIPLIER) {
            currentMultiplier = intent.getFloatExtra(EXTRA_MULTIPLIER, 2.0f)
            updateNotification()
        }
        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        // Repornire dacă e ucis din recents
        val restartIntent = Intent(applicationContext, StepBoosterService::class.java)
        val pendingIntent = PendingIntent.getService(
            this, 1, restartIntent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        alarmManager.set(
            AlarmManager.ELAPSED_REALTIME,
            SystemClock.elapsedRealtime() + 2000,
            pendingIntent
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        sensorManager.unregisterListener(this)
        serviceScope.cancel()
        if (wakeLock.isHeld) wakeLock.release()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    // ── Sensor ──────────────────────────────────────────────────────────────

    private fun registerStepSensor() {
        val stepDetector = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
        if (stepDetector != null) {
            sensorManager.registerListener(this, stepDetector, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_STEP_DETECTOR) {
            val now = Instant.now()
            synchronized(this) {
                batchRealSteps++
                realStepsToday++
                if (batchStartTime == null) batchStartTime = now
                batchEndTime = now
            }
            startBatchTimerIfNeeded()
            updateNotification()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    // ── Batching ─────────────────────────────────────────────────────────────

    private fun startBatchTimerIfNeeded() {
        if (batchJob?.isActive == true) return
        batchJob = serviceScope.launch {
            delay(BATCH_INTERVAL_MS)
            flushBatch()
        }
    }

    private suspend fun flushBatch() {
        val steps: Long
        val start: Instant
        val end: Instant
        synchronized(this) {
            if (batchRealSteps == 0L) return
            steps = batchRealSteps
            start = batchStartTime ?: Instant.now()
            end = batchEndTime ?: Instant.now()
            batchRealSteps = 0L
            batchStartTime = null
            batchEndTime = null
        }
        val boosted = Math.round(steps * currentMultiplier)
        boostedStepsToday += boosted
        healthConnectManager.writeSteps(boosted, start, end)
        updateNotification()
    }

    // ── Notification ─────────────────────────────────────────────────────────

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            setShowBadge(false)
        }
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pi = PendingIntent.getActivity(this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("StepBooster ${currentMultiplier}x Activ")
            .setContentText("Pași reali: $realStepsToday | HC: $boostedStepsToday")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentIntent(pi)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    private fun updateNotification() {
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(NOTIFICATION_ID, buildNotification())
    }

    // ── Watchdog ─────────────────────────────────────────────────────────────

    private fun scheduleWatchdog() {
        val intent = Intent(this, WatchdogAlarmReceiver::class.java)
        val pi = PendingIntent.getBroadcast(this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val am = getSystemService(ALARM_SERVICE) as AlarmManager
        am.setExactAndAllowWhileIdle(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            SystemClock.elapsedRealtime() + WATCHDOG_INTERVAL_MS,
            pi
        )
    }
}
