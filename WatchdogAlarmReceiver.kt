package com.example.stepbooster

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class WatchdogAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        CoroutineScope(Dispatchers.IO).launch {
            val enabled = MultiplierDataStore.getServiceEnabled(context).first()
            if (enabled && !StepBoosterService.isRunning) {
                StepBoosterService.start(context)
            }
            // Reprogramează watchdog-ul
            if (enabled) {
                val nextIntent = Intent(context, WatchdogAlarmReceiver::class.java)
                val pi = PendingIntent.getBroadcast(context, 0, nextIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                am.setExactAndAllowWhileIdle(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + 5 * 60 * 1000L,
                    pi
                )
            }
        }
    }
}
