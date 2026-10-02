package com.scootah.app.navigation

sealed class Screen(val route: String) {
    // Onboarding
    data object Welcome : Screen("welcome")
    data object Setup : Screen("setup")

    // Main (bottom nav host)
    data object Main : Screen("main")

    // Bottom nav tabs
    data object Garage : Screen("garage")
    data object Home : Screen("home")
    data object Map : Screen("map")
}
