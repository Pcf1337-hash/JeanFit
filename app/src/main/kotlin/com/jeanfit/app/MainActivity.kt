package com.jeanfit.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.jeanfit.app.navigation.BottomNavItem
import com.jeanfit.app.navigation.JeanFitNavGraph
import com.jeanfit.app.navigation.Screen
import com.jeanfit.app.ui.components.JeanFitBottomBar
import com.jeanfit.app.ui.theme.JeanFitTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: MainViewModel = hiltViewModel()
            val isDarkMode by viewModel.isDarkMode.collectAsState(initial = false)
            val onboardingCompleted by viewModel.onboardingCompleted.collectAsState(initial = false)

            JeanFitTheme(darkTheme = isDarkMode) {
                val navController = rememberNavController()
                val startDestination = if (onboardingCompleted)
                    Screen.Home.route else Screen.Welcome.route

                val mainRoutes = BottomNavItem.items.map { it.screen.route }.toSet()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        JeanFitBottomBar(
                            navController = navController,
                            items = BottomNavItem.items,
                            visibleOnRoutes = mainRoutes
                        )
                    }
                ) { _ ->
                    JeanFitNavGraph(
                        navController = navController,
                        startDestination = startDestination
                    )
                }
            }
        }
    }
}
