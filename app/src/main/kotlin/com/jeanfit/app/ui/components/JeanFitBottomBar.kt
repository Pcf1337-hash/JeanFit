package com.jeanfit.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Badge
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.jeanfit.app.navigation.BottomNavItem
import com.jeanfit.app.navigation.Screen
import com.jeanfit.app.ui.theme.CoachCardDark
import com.jeanfit.app.ui.theme.OceanBlue
import com.jeanfit.app.ui.theme.SkyBlue

@Composable
fun JeanFitBottomBar(
    navController: NavController,
    items: List<BottomNavItem>,
    visibleOnRoutes: Set<String>,
    coachUnreadCount: Int = 0
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val isVisible = currentRoute in visibleOnRoutes

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it })
    ) {
        NavigationBar(
            containerColor = CoachCardDark
        ) {
            items.forEach { item ->
                val isSelected = currentRoute == item.screen.route
                NavigationBarItem(
                    selected = isSelected,
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
                        if (item is BottomNavItem.Coach && coachUnreadCount > 0) {
                            BadgedBox(
                                badge = {
                                    Badge(containerColor = SkyBlue) {
                                        Text(
                                            if (coachUnreadCount > 9) "9+" else coachUnreadCount.toString()
                                        )
                                    }
                                }
                            ) {
                                Icon(imageVector = item.icon, contentDescription = item.label)
                            }
                        } else {
                            Icon(imageVector = item.icon, contentDescription = item.label)
                        }
                    },
                    label = { Text(item.label) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = SkyBlue,
                        selectedTextColor = SkyBlue,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        indicatorColor = OceanBlue.copy(alpha = 0.2f)
                    )
                )
            }
        }
    }
}
