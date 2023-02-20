package com.ramitsuri.expensereports.android.ui

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ramitsuri.expensereports.android.ui.settings.SettingsScreen

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SettingsNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    showSettingsNavGraph: MutableState<Boolean>,
    startDestination: String = SETTINGS_SCREEN_ROUTE
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = startDestination
    ) {
        composable(route = SETTINGS_SCREEN_ROUTE) {
            SettingsScreen(
                onBack = {
                    showSettingsNavGraph.value = false
                    navController.popBackStack()
                }
            )
        }
    }
}

private const val SETTINGS_SCREEN_ROUTE = "settings_screen_route"