package com.ramitsuri.expensereports.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import expensereports.shared.generated.resources.Res
import expensereports.shared.generated.resources.overview
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    state: DashboardUiState,
    onNavigateToReport: (reportName: String) -> Unit,
    onNavigateToTransactions: (filterTag: String?) -> Unit,
    onRefresh: () -> Unit,
    onToggleMask: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPullToRefreshAvailable: Boolean = true,
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .statusBarsPadding()
                .displayCutoutPadding(),
    ) {
        CenterAlignedTopAppBar(
            scrollBehavior = scrollBehavior,
            colors =
                TopAppBarDefaults.topAppBarColors().copy(
                    scrolledContainerColor = MaterialTheme.colorScheme.background,
                ),
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(Res.string.overview),
                        style = MaterialTheme.typography.titleLarge,
                    )
                    if (state.selectedPeriod.isNotEmpty()) {
                        Text(
                            text = state.selectedPeriod,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            },
            actions = {
                IconButton(
                    onClick = {
                        if (!state.isLoading) {
                            onRefresh()
                        }
                    },
                    modifier =
                        Modifier
                            .size(48.dp)
                            .padding(4.dp),
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.Refresh,
                            contentDescription = "Refresh",
                        )
                    }
                }
                IconButton(
                    onClick = onSettingsClick,
                    modifier =
                        Modifier
                            .size(48.dp)
                            .padding(4.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = "Settings",
                    )
                }
            },
        )

        PullToRefreshBox(
            isRefreshing = isPullToRefreshAvailable && state.isLoading,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxSize(),
        ) {
            LazyColumn(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .nestedScroll(scrollBehavior.nestedScrollConnection),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                if (state.netWorth != null && state.netWorth.isAvailable) {
                    item(key = "net_worth") {
                        NetWorthHeroCard(
                            summary = state.netWorth,
                            isMasked = state.isSensitiveDataMasked,
                            onToggleMask = onToggleMask,
                            onClick = { onNavigateToReport("Net Worth") },
                        )
                    }
                }

                item(key = "working_capital") {
                    WorkingCapitalCard(summary = state.workingCapital)
                }

                item(key = "cash_flow") {
                    CashFlowHeroCard(summary = state.monthlyCashFlow)
                }

                item(key = "top_expenses") {
                    TopExpensesCard(
                        categories = state.topSpendingCategories,
                        onViewFullExpensesClick = { onNavigateToReport("After Deduction Expenses") },
                    )
                }

                item(key = "annual_goals") {
                    AnnualGoalsCard(goals = state.annualContributions)
                }

                if (state.activeTxGroups.isNotEmpty()) {
                    item(key = "active_trips") {
                        ActiveTripsCard(
                            trips = state.activeTxGroups,
                            onTripClick = { tripId -> onNavigateToTransactions(tripId) },
                        )
                    }
                }

                if (state.recentTransactionsPreview.isNotEmpty()) {
                    item(key = "recent_transactions") {
                        RecentTransactionsCard(
                            transactions = state.recentTransactionsPreview,
                            onViewAllClick = { onNavigateToTransactions(null) },
                        )
                    }
                }

                item(key = "bottom_spacer") {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}
