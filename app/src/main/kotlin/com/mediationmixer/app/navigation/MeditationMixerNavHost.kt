package com.mediationmixer.app.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mediationmixer.app.ui.home.HomeScreen
import com.mediationmixer.app.ui.library.LibraryScreen
import com.mediationmixer.app.ui.mixer.MixerScreen
import com.mediationmixer.app.ui.settings.SettingsScreen
import com.mediationmixer.app.ui.streaks.StreaksScreen

@Composable
fun MeditationMixerNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute != Screen.Mixer.route && currentRoute != Screen.Presets.route

    Scaffold(
        modifier = modifier,
        bottomBar = {
            if (showBottomBar) {
                MeditationBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToMixer = { navController.navigate(Screen.Mixer.route) }
                )
            }
            composable(Screen.Mixer.route) {
                MixerScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Library.route) {
                LibraryScreen()
            }
            composable(Screen.Streaks.route) {
                StreaksScreen()
            }
            composable(Screen.Settings.route) {
                SettingsScreen()
            }
        }
    }
}

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Mixer : Screen("mixer")
    data object Library : Screen("library")
    data object Streaks : Screen("streaks")
    data object Settings : Screen("settings")
}
