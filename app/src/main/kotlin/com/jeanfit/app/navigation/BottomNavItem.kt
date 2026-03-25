package com.jeanfit.app.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val screen: Screen,
    val label: String,
    val icon: ImageVector
) {
    data object Home : BottomNavItem(Screen.Home, "Home", Icons.Filled.Home)
    data object Progress : BottomNavItem(Screen.Progress, "Fortschritt", Icons.Filled.ShowChart)
    data object Tools : BottomNavItem(Screen.Tools, "Tools", Icons.Filled.FitnessCenter)
    data object Learn : BottomNavItem(Screen.Learn, "Lernen", Icons.Filled.MenuBook)

    companion object {
        val items = listOf(Home, Progress, Tools, Learn)
    }
}
