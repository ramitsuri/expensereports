package com.ramitsuri.expensereports.android.ui

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.ramitsuri.expensereports.android.ui.expenses.ExpenseReportScreen
import com.ramitsuri.expensereports.android.ui.settings.SettingsScreen

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun NavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberAnimatedNavController(),
    startDestination: String = EXPENSE_SCREEN_ROUTE
) {
    AnimatedNavHost(
        modifier = modifier,
        navController = navController,
        startDestination = startDestination,
        enterTransition = { slideIntoContainer(AnimatedContentScope.SlideDirection.Left) },
        exitTransition = { slideOutOfContainer(AnimatedContentScope.SlideDirection.Right) }
    ) {
        expenseNavigation(
            onNavigateToSettings = {
                navController.navigateToSettings()
            }
        )

        settingsNavigation()
    }
}

@OptIn(ExperimentalAnimationApi::class)
private fun NavGraphBuilder.expenseNavigation(
    onNavigateToSettings: () -> Unit
) {
    composable(route = EXPENSE_SCREEN_ROUTE) {
        ExpenseReportScreen(onNavigateToSettings = onNavigateToSettings)
    }
}

@OptIn(ExperimentalAnimationApi::class)
private fun NavGraphBuilder.settingsNavigation() {
    composable(route = SETTINGS_SCREEN_ROUTE) {
        SettingsScreen()
    }
}

private fun NavController.navigateToSettings() {
    this.navigate(SETTINGS_SCREEN_ROUTE)
}

private const val EXPENSE_SCREEN_ROUTE = "expense_screen_route"
private const val SETTINGS_SCREEN_ROUTE = "settings_screen_route"