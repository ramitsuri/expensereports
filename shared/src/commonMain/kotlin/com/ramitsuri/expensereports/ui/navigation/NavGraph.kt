package com.ramitsuri.expensereports.ui.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import com.ramitsuri.expensereports.ui.home.HomeScreen
import com.ramitsuri.expensereports.ui.home.HomeViewModel
import com.ramitsuri.expensereports.ui.report.ReportScreen
import com.ramitsuri.expensereports.ui.report.ReportViewModel
import com.ramitsuri.expensereports.ui.settings.SettingsScreen
import com.ramitsuri.expensereports.ui.settings.SettingsViewModel
import com.ramitsuri.expensereports.ui.theme.AppTheme
import com.ramitsuri.expensereports.ui.transactions.TransactionsScreen
import com.ramitsuri.expensereports.ui.transactions.TransactionsViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun NavGraph(
    modifier: Modifier = Modifier,
    windowSize: WindowSizeClass,
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
                modifier =
                    Modifier.padding(
                        top = innerPadding.calculateTopPadding(),
                        bottom = innerPadding.calculateBottomPadding(),
                    ),
            ) {
                composable<Destination.Home>(
                    deepLinks =
                        listOf(
                            navDeepLink<Destination.Home>(basePath = Destination.Home.deepLinkUri),
                        ),
                ) {
                    val viewModel = koinViewModel<HomeViewModel>()
                    val viewState by viewModel.viewState.collectAsStateWithLifecycle()
                    HomeScreen(
                        viewState = viewState,
                        onNetWorthPeriodSelected = viewModel::onNetWorthPeriodSelected,
                        windowSize = windowSize,
                        onReportsClick = { navController.navigate(Destination.Report) },
                        onTransactionsClick = { navController.navigate(Destination.Transactions) },
                        onSettingsClick = { navController.navigate(Destination.Settings) },
                        onRefresh = viewModel::onRefresh,
                    )
                }

                composable<Destination.Settings> {
                    val viewModel = koinViewModel<SettingsViewModel>()
                    val viewState by viewModel.viewState.collectAsStateWithLifecycle()
                    SettingsScreen(
                        viewState = viewState,
                        onBack = { navController.navigateUp() },
                        onUrlSet = viewModel::setBaseUrl,
                    )
                }

                composable<Destination.Report> {
                    val viewModel = koinViewModel<ReportViewModel>()
                    val viewState by viewModel.viewState.collectAsStateWithLifecycle()
                    ReportScreen(
                        viewState = viewState,
                        onBack = { navController.navigateUp() },
                        onPeriodSelected = viewModel::onPeriodSelected,
                        onReportSelected = viewModel::onReportSelected,
                    )
                }

                composable<Destination.Transactions> {
                    val viewModel = koinViewModel<TransactionsViewModel>()
                    val viewState by viewModel.viewState.collectAsStateWithLifecycle()
                    TransactionsScreen(
                        viewState = viewState,
                        windowSize = windowSize,
                        onBack = { navController.navigateUp() },
                        onFilterApplied = viewModel::onFilterApplied,
                    )
                }
            }
        }
    }
}
