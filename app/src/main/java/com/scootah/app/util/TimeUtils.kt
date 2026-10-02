package com.scootah.app.util

import java.util.Calendar

fun getGreeting(userName: String): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when {
        hour in 5..11  -> "Günaydın, $userName! ☀️"
        hour in 12..17 -> "İyi günler, $userName! 🌤️"
        hour in 18..21 -> "İyi akşamlar, $userName! 🌇"
        else           -> "İyi geceler, $userName! 🌙"
    }
}
