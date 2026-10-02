package com.scootah.app.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import com.scootah.app.data.local.PreferencesKeys
import com.scootah.app.data.local.appDataStore
import com.scootah.app.data.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

/**
 * Single source of truth for UserProfile.
 * Backed by DataStore — survives app restarts.
 */
class UserRepository private constructor(private val context: Context) {

    /**
     * Live stream of the user's profile from disk.
     * Emits immediately with default values if no data saved yet.
     */
    val userProfile: Flow<UserProfile> = context.appDataStore.data
        .catch { exception ->
            // If disk read fails, emit empty defaults rather than crashing
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { prefs ->
            val totalKm = prefs[PreferencesKeys.TOTAL_KM] ?: 20f
            UserProfile(
                userName = prefs[PreferencesKeys.USER_NAME] ?: "",
                scooterName = prefs[PreferencesKeys.SCOOTER_NAME] ?: "",
                streakDays = prefs[PreferencesKeys.STREAK_DAYS] ?: 5,
                totalKm = totalKm,
                co2SavedKg = totalKm * 0.12f,
                fuelSavedLiters = totalKm * 0.045f,
                isLoggedIn = prefs[PreferencesKeys.ONBOARDING_COMPLETE] ?: false
            )
        }

    /** Returns true if the user has completed setup at least once. */
    suspend fun isOnboardingComplete(): Boolean =
        context.appDataStore.data
            .map { prefs -> prefs[PreferencesKeys.ONBOARDING_COMPLETE] ?: false }
            .first()

    /** Persist the profile and mark onboarding as done. */
    suspend fun saveProfile(userName: String, scooterName: String) {
        context.appDataStore.edit { prefs ->
            prefs[PreferencesKeys.USER_NAME] = userName
            prefs[PreferencesKeys.SCOOTER_NAME] = scooterName
            prefs[PreferencesKeys.ONBOARDING_COMPLETE] = true
        }
    }

    /** Accumulate driven km and update derived stats. */
    suspend fun addKm(km: Float) {
        if (km <= 0f) return
        context.appDataStore.edit { prefs ->
            val current = prefs[PreferencesKeys.TOTAL_KM] ?: 0f
            prefs[PreferencesKeys.TOTAL_KM] = current + km
        }
    }

    // --- Singleton ---
    companion object {
        @Volatile private var INSTANCE: UserRepository? = null

        fun getInstance(context: Context): UserRepository =
            INSTANCE ?: synchronized(this) {
                UserRepository(context.applicationContext).also { INSTANCE = it }
            }
    }
}
