package com.example.stepbooster

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AppState(
    val isServiceRunning: Boolean = false,
    val multiplier: Float = 2.0f,
    val realStepsToday: Long = 0L,
    val boostedStepsToday: Long = 0L,
    val healthConnectAvailable: Boolean = false,
    val hasPermissions: Boolean = false
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val healthConnectManager = HealthConnectManager(context)

    private val _state = MutableStateFlow(AppState())
    val state: StateFlow<AppState> = _state.asStateFlow()

    init {
        // Citim multiplicatorul salvat
        viewModelScope.launch {
            MultiplierDataStore.getMultiplier(context).collect { mult ->
                _state.value = _state.value.copy(multiplier = mult)
            }
        }
        viewModelScope.launch {
            MultiplierDataStore.getServiceEnabled(context).collect { enabled ->
                _state.value = _state.value.copy(isServiceRunning = enabled && StepBoosterService.isRunning)
            }
        }

        // Refresh stare la fiecare secundă
        viewModelScope.launch {
            while (true) {
                delay(1000)
                _state.value = _state.value.copy(
                    isServiceRunning = StepBoosterService.isRunning,
                    realStepsToday = StepBoosterService.realStepsToday,
                    boostedStepsToday = StepBoosterService.boostedStepsToday,
                    healthConnectAvailable = healthConnectManager.isAvailable()
                )
            }
        }

        // Check permisiuni
        viewModelScope.launch {
            val avail = healthConnectManager.isAvailable()
            val perms = if (avail) healthConnectManager.hasPermissions() else false
            _state.value = _state.value.copy(
                healthConnectAvailable = avail,
                hasPermissions = perms
            )
        }
    }

    fun toggleService() {
        viewModelScope.launch {
            if (StepBoosterService.isRunning) {
                StepBoosterService.stop(context)
                MultiplierDataStore.setServiceEnabled(context, false)
                _state.value = _state.value.copy(isServiceRunning = false)
            } else {
                StepBoosterService.start(context)
                MultiplierDataStore.setServiceEnabled(context, true)
                _state.value = _state.value.copy(isServiceRunning = true)
            }
        }
    }

    fun setMultiplier(value: Float) {
        val rounded = (Math.round(value * 2) / 2.0f) // Snap la 0.5
        viewModelScope.launch {
            MultiplierDataStore.setMultiplier(context, rounded)
            StepBoosterService.currentMultiplier = rounded
            // Trimite update la serviciu dacă rulează
            if (StepBoosterService.isRunning) {
                val intent = Intent(context, StepBoosterService::class.java).apply {
                    action = StepBoosterService.ACTION_UPDATE_MULTIPLIER
                    putExtra(StepBoosterService.EXTRA_MULTIPLIER, rounded)
                }
                context.startService(intent)
            }
        }
        _state.value = _state.value.copy(multiplier = rounded)
    }

    fun onPermissionsGranted() {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                hasPermissions = healthConnectManager.hasPermissions()
            )
        }
    }
}
