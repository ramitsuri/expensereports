package com.ramitsuri.expensereports.ui.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ramitsuri.expensereports.ui.home.HomeScreen
import com.ramitsuri.expensereports.ui.home.HomeViewModel
import com.ramitsuri.expensereports.ui.settings.SettingsScreen
import com.ramitsuri.expensereports.ui.theme.AppTheme
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun NavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    dynamicDarkColorScheme: ColorScheme? = null,
    dynamicLightColorScheme: ColorScheme? = null,
) {
    AppTheme(
        dynamicDarkColorScheme = dynamicDarkColorScheme,
        dynamicLightColorScheme = dynamicLightColorScheme,
    ) {
        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            modifier =
                modifier
                    .fillMaxSize(),
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Destination.Home,
                modifier = Modifier.padding(
                    top = innerPadding.calculateTopPadding(),
                    bottom = innerPadding.calculateBottomPadding(),
                ),
            ) {
                composable<Destination.Home> {
                    val viewModel = koinViewModel<HomeViewModel>()
                    val viewState by viewModel.viewState.collectAsStateWithLifecycle()
                    HomeScreen(
                        viewState = viewState,
                        onNetWorthPeriodSelected = viewModel::onNetWorthPeriodSelected,
                    )
                }

                composable<Destination.Settings> {
                    SettingsScreen()
                }
            }
        }
    }
}
