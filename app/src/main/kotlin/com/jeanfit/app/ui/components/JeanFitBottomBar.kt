package com.jeanfit.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.jeanfit.app.navigation.BottomNavItem
import com.jeanfit.app.ui.theme.SunsetOrange

@Composable
fun JeanFitBottomBar(
    navController: NavController,
    items: List<BottomNavItem>,
    visibleOnRoutes: Set<String>
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val isVisible = currentRoute in visibleOnRoutes

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it })
    ) {
        NavigationBar {
            items.forEach { item ->
                NavigationBarItem(
                    selected = currentRoute == item.screen.route,
                    onClick = {
                        if (currentRoute != item.screen.route) {
                            navController.navigate(item.screen.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label
                        )
                    },
                    label = { Text(item.label) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = SunsetOrange,
                        selectedTextColor = SunsetOrange,
                        indicatorColor = SunsetOrange.copy(alpha = 0.12f)
                    )
                )
            }
        }
    }
}
