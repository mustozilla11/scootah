package com.scootah.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

/**
 * Single DataStore instance for the entire app.
 * Access via [Context.appDataStore] extension property.
 */
val Context.appDataStore: DataStore<Preferences> by preferencesDataStore(name = "scootah_prefs")

/**
 * Typed keys for all persisted preferences.
 */
object PreferencesKeys {
    val USER_NAME = stringPreferencesKey("user_name")
    val SCOOTER_NAME = stringPreferencesKey("scooter_name")
    val STREAK_DAYS = intPreferencesKey("streak_days")
    val TOTAL_KM = floatPreferencesKey("total_km")
    val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
}
