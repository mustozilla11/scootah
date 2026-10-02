package com.scootah.app.data.model

data class UserProfile(
    val userName: String = "",
    val scooterName: String = "",
    val totalKm: Float = 0f,
    val streakDays: Int = 5,
    val co2SavedKg: Float = 2.4f,
    val fuelSavedLiters: Float = 1.1f,
    val isLoggedIn: Boolean = false
)
