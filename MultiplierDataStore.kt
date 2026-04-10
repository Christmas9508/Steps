package com.example.stepbooster

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "stepbooster_prefs")

object MultiplierDataStore {
    private val MULTIPLIER_KEY = floatPreferencesKey("multiplier")
    private val SERVICE_ENABLED_KEY = booleanPreferencesKey("service_enabled")

    fun getMultiplier(context: Context): Flow<Float> =
        context.dataStore.data.map { prefs -> prefs[MULTIPLIER_KEY] ?: 2.0f }

    fun getServiceEnabled(context: Context): Flow<Boolean> =
        context.dataStore.data.map { prefs -> prefs[SERVICE_ENABLED_KEY] ?: false }

    suspend fun setMultiplier(context: Context, value: Float) {
        context.dataStore.edit { prefs -> prefs[MULTIPLIER_KEY] = value }
    }

    suspend fun setServiceEnabled(context: Context, enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[SERVICE_ENABLED_KEY] = enabled }
    }
}
