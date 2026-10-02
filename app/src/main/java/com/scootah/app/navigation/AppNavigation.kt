package com.scootah.app.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.scootah.app.data.repository.UserRepository
import com.scootah.app.ui.onboarding.SetupScreen
import com.scootah.app.ui.onboarding.WelcomeScreen
import com.scootah.app.ui.theme.ScootahGreen

@Composable
fun AppNavigation() {
    val context = LocalContext.current
    val navController = rememberNavController()

    // null = still loading, non-null = route determined
    var startDestination by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val isComplete = UserRepository.getInstance(context).isOnboardingComplete()
        startDestination = if (isComplete) Screen.Main.route else Screen.Welcome.route
    }

    // Show a minimal splash while we read DataStore (usually < 100ms)
    if (startDestination == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = ScootahGreen)
        }
        return
    }

    NavHost(
        navController = navController,
        startDestination = startDestination!!
    ) {
        composable(Screen.Welcome.route) {
            WelcomeScreen(
                onContinueWithGoogle = {
                    navController.navigate(Screen.Setup.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Setup.route) {
            SetupScreen(
                onSetupComplete = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Setup.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Main.route) {
            MainScreen()
        }
    }
}
