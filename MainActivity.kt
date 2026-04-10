package com.example.stepbooster

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.health.connect.client.HealthConnectClient
import com.example.stepbooster.ui.MainScreen
import com.example.stepbooster.ui.theme.StepBoosterTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    // Permisiune ACTIVITY_RECOGNITION (Android 10+)
    private val activityRecognitionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) requestHealthConnectPermissions()
    }

    // Permisiuni Health Connect
    private val healthPermissionsLauncher = registerForActivityResult(
        HealthConnectClient.requestPermissionsActivityContract()
    ) { _ ->
        viewModel.onPermissionsGranted()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StepBoosterTheme {
                val state by viewModel.state.collectAsState()
                MainScreen(
                    state = state,
                    onToggleService = { viewModel.toggleService() },
                    onMultiplierChange = { viewModel.setMultiplier(it) },
                    onRequestPermissions = { requestAllPermissions() },
                    onBatteryOptimization = { openBatterySettings() },
                    onOpenHealthConnectStore = { openPlayStore("com.google.android.apps.healthdata") }
                )
            }
        }
    }

    private fun requestAllPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            activityRecognitionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
        } else {
            requestHealthConnectPermissions()
        }
    }

    private fun requestHealthConnectPermissions() {
        val hcManager = HealthConnectManager(this)
        if (hcManager.isAvailable()) {
            healthPermissionsLauncher.launch(hcManager.permissions)
        }
    }

    private fun openBatterySettings() {
        try {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:$packageName")
            }
            startActivity(intent)
        } catch (e: Exception) {
            startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
        }
    }

    private fun openPlayStore(packageName: String) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
        } catch (e: Exception) {
            startActivity(Intent(Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=$packageName")))
        }
    }
}
