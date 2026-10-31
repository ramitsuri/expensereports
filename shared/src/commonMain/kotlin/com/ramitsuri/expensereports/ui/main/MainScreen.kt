package com.ramitsuri.expensereports.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ramitsuri.expensereports.ui.dashboard.DashboardScreen
import com.ramitsuri.expensereports.ui.dashboard.DashboardViewModel
import com.ramitsuri.expensereports.ui.report.ReportScreen
import com.ramitsuri.expensereports.ui.report.ReportViewModel
import com.ramitsuri.expensereports.ui.transactions.TransactionsScreen
import com.ramitsuri.expensereports.ui.transactions.TransactionsViewModel
import expensereports.shared.generated.resources.Res
import expensereports.shared.generated.resources.activity
import expensereports.shared.generated.resources.overview
import expensereports.shared.generated.resources.reports
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

enum class MainTab {
    OVERVIEW,
    REPORTS,
    ACTIVITY,
}

@Composable
fun MainScreen(
    windowSize: WindowSizeClass,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
    initialTab: MainTab = MainTab.OVERVIEW,
) {
    var currentTab by rememberSaveable { mutableStateOf(initialTab) }

    val dashboardViewModel = koinViewModel<DashboardViewModel>()
    val reportViewModel = koinViewModel<ReportViewModel>()
    val transactionsViewModel = koinViewModel<TransactionsViewModel>()

    val dashboardState by dashboardViewModel.viewState.collectAsStateWithLifecycle()
    val reportState by reportViewModel.viewState.collectAsStateWithLifecycle()
    val transactionsState by transactionsViewModel.viewState.collectAsStateWithLifecycle()

    val saveableStateHolder = rememberSaveableStateHolder()

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentTab == MainTab.OVERVIEW,
                    onClick = { currentTab = MainTab.OVERVIEW },
                    icon = {
                        Icon(
                            imageVector =
                                if (currentTab == MainTab.OVERVIEW) {
                                    Icons.Filled.Dashboard
                                } else {
                                    Icons.Outlined.Dashboard
                                },
                            contentDescription = stringResource(Res.string.overview),
                        )
                    },
                    label = { Text(stringResource(Res.string.overview)) },
                )
                NavigationBarItem(
                    selected = currentTab == MainTab.REPORTS,
                    onClick = { currentTab = MainTab.REPORTS },
                    icon = {
                        Icon(
                            imageVector =
                                if (currentTab == MainTab.REPORTS) {
                                    Icons.Filled.Description
                                } else {
                                    Icons.Outlined.Description
                                },
                            contentDescription = stringResource(Res.string.reports),
                        )
                    },
                    label = { Text(stringResource(Res.string.reports)) },
                )
                NavigationBarItem(
                    selected = currentTab == MainTab.ACTIVITY,
                    onClick = { currentTab = MainTab.ACTIVITY },
                    icon = {
                        Icon(
                            imageVector =
                                if (currentTab == MainTab.ACTIVITY) {
                                    Icons.AutoMirrored.Filled.List
                                } else {
                                    Icons.AutoMirrored.Outlined.List
                                },
                            contentDescription = stringResource(Res.string.activity),
                        )
                    },
                    label = { Text(stringResource(Res.string.activity)) },
                )
            }
        },
        modifier = modifier.fillMaxSize(),
    ) { innerPadding ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(bottom = innerPadding.calculateBottomPadding()),
        ) {
            saveableStateHolder.SaveableStateProvider(currentTab) {
                when (currentTab) {
                    MainTab.OVERVIEW -> {
                        DashboardScreen(
                            state = dashboardState,
                            onNavigateToReport = { reportName ->
                                reportViewModel.onReportSelected(reportName)
                                currentTab = MainTab.REPORTS
                            },
                            onNavigateToTransactions = { filterTag ->
                                transactionsViewModel.filterByTag(filterTag)
                                currentTab = MainTab.ACTIVITY
                            },
                            onRefresh = dashboardViewModel::onRefresh,
                            onToggleMask = dashboardViewModel::togglePrivacyMask,
                            onSettingsClick = onSettingsClick,
                            isPullToRefreshAvailable = !dashboardViewModel.isDesktop,
                        )
                    }

                    MainTab.REPORTS -> {
                        ReportScreen(
                            viewState = reportState,
                            onBack = { currentTab = MainTab.OVERVIEW },
                            onPeriodSelected = reportViewModel::onPeriodSelected,
                            onReportSelected = reportViewModel::onReportSelected,
                        )
                    }

                    MainTab.ACTIVITY -> {
                        TransactionsScreen(
                            viewState = transactionsState,
                            windowSize = windowSize,
                            onBack = { currentTab = MainTab.OVERVIEW },
                            onFilterApplied = transactionsViewModel::onFilterApplied,
                            onFromAccountTextUpdated = transactionsViewModel::onFromAccountTextUpdated,
                            onToAccountTextUpdated = transactionsViewModel::onToAccountTextUpdated,
                        )
                    }
                }
            }
        }
    }
}
