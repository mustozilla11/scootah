package com.scootah.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.scootah.app.navigation.AppNavigation
import com.scootah.app.ui.theme.ScootahTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ScootahTheme {
                AppNavigation()
            }
        }
    }
}
