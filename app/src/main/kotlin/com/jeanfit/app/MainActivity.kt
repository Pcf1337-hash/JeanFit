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
import com.jeanfit.app.ui.update.UpdateBottomSheet
import com.jeanfit.app.ui.update.UpdateViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val mainViewModel: MainViewModel = hiltViewModel()
            val updateViewModel: UpdateViewModel = hiltViewModel()

            val isDarkMode by mainViewModel.isDarkMode.collectAsState(initial = false)
            val onboardingCompleted by mainViewModel.onboardingCompleted.collectAsState(initial = false)
            val coachUnread by mainViewModel.coachUnreadCount.collectAsState(initial = 0)
            val updateState by updateViewModel.state.collectAsState()

            JeanFitTheme(darkTheme = isDarkMode) {
                val navController = rememberNavController()
                val startDestination = if (onboardingCompleted)
                    Screen.Home.route else "onboarding_graph"

                val mainRoutes = BottomNavItem.items.map { it.screen.route }.toSet()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        JeanFitBottomBar(
                            navController = navController,
                            items = BottomNavItem.items,
                            visibleOnRoutes = mainRoutes,
                            coachUnreadCount = coachUnread
                        )
                    }
                ) { _ ->
                    JeanFitNavGraph(
                        navController = navController,
                        startDestination = startDestination
                    )
                }

                // Update-Dialog — zeigt sich automatisch oder auf manuelle Anfrage
                if (updateState.isVisible && updateState.release != null) {
                    UpdateBottomSheet(
                        release = updateState.release!!,
                        isDownloading = updateState.isDownloading,
                        downloadProgress = updateState.downloadProgress,
                        currentVersion = updateViewModel.getCurrentVersion(),
                        error = updateState.error,
                        onUpdate = { updateViewModel.startDownloadAndInstall() },
                        onDismiss = { updateViewModel.dismiss() },
                        onSkip = { updateViewModel.skipVersion() }
                    )
                }
            }
        }
    }
}
