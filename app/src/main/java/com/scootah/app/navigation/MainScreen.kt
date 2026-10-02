package com.scootah.app.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.scootah.app.ui.garage.GarageScreen
import com.scootah.app.ui.home.HomeScreen
import com.scootah.app.ui.map.MapScreen

private data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

private val bottomNavItems = listOf(
    BottomNavItem("Garaj", Icons.Filled.TwoWheeler, Screen.Garage.route),
    BottomNavItem("Ana Sayfa", Icons.Filled.Home, Screen.Home.route),
    BottomNavItem("Sürüş", Icons.Filled.DirectionsRun, Screen.Map.route),
)

@Composable
fun MainScreen(
    initialTab: Int = 1
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(initialTab) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        when (selectedTab) {
            0 -> GarageScreen(modifier = Modifier.padding(innerPadding))
            1 -> HomeScreen(
                modifier = Modifier.padding(innerPadding),
                onStartRide = { selectedTab = 2 }
            )
            2 -> MapScreen(modifier = Modifier.padding(innerPadding))
        }
    }
}
