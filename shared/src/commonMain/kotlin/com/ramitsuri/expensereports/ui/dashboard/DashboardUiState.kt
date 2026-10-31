package com.ramitsuri.expensereports.ui.dashboard

data class DashboardUiState(
    // e.g., "October 2026"
    val selectedPeriod: String,
    val netWorth: NetWorthSummary? = null,
    val isSensitiveDataMasked: Boolean = true,
    val workingCapital: WorkingCapitalSummary,
    val monthlyCashFlow: CashFlowSummary,
    val topSpendingCategories: List<CategorySpendItem>,
    val annualContributions: List<AnnualContributionProgress>,
    val activeTxGroups: List<TxGroupSummary> = emptyList(),
    val recentTransactionsPreview: List<TransactionPreviewItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
) {
    companion object {
        val EMPTY =
            DashboardUiState(
                selectedPeriod = "",
                netWorth = null,
                isSensitiveDataMasked = true,
                workingCapital =
                    WorkingCapitalSummary(
                        totalCash = 0.0,
                        totalCreditDebt = 0.0,
                        netLiquidity = 0.0,
                        accounts = emptyList(),
                    ),
                monthlyCashFlow =
                    CashFlowSummary(
                        incomeWithoutGains = 0.0,
                        afterTaxIncome = 0.0,
                        livingExpenses = 0.0,
                        netDelta = 0.0,
                        savingsRatePercent = null,
                        trailingNetDeltas = emptyList(),
                        trailingSavingsRates = emptyList(),
                    ),
                topSpendingCategories = emptyList(),
                annualContributions = emptyList(),
                activeTxGroups = emptyList(),
                recentTransactionsPreview = emptyList(),
                isLoading = false,
                error = null,
            )
    }
}

data class NetWorthSummary(
    val currentNetWorth: Double,
    // currentNetWorth - previousMonthNetWorth
    val previousMonthDelta: Double,
    // (delta / previousMonthNetWorth) * 100
    val previousMonthDeltaPercent: Double,
    // Historical monthly trend points
    val trailingHistory: List<TrendPoint>,
    val isAvailable: Boolean = true,
)

data class TrendPoint(
    // e.g., "Jun", "Jul", "Aug"
    val periodLabel: String,
    val value: Double,
    val fullLabel: String = periodLabel,
    val subLabel: String = "",
)

data class WorkingCapitalSummary(
    val totalCash: Double,
    val totalCreditDebt: Double,
    // totalCash - totalCreditDebt
    val netLiquidity: Double,
    val accounts: List<AccountBalanceItem>,
)

data class CashFlowSummary(
    val incomeWithoutGains: Double,
    val afterTaxIncome: Double = 0.0,
    // After-deduction expenses
    val livingExpenses: Double,
    // After-tax cash flow: afterTaxIncome - livingExpenses
    val netDelta: Double,
    val savingsRatePercent: Double?,
    // Multi-month sparkline data
    val trailingNetDeltas: List<TrendPoint>,
    // Multi-month trend points
    val trailingSavingsRates: List<TrendPoint>,
)

data class CategorySpendItem(
    val categoryName: String,
    val currentAmount: Double,
    // Average across past months in payload
    val trailingAverageAmount: Double?,
    val percentageOfTotal: Double,
)

data class AnnualContributionProgress(
    val name: String,
    val currentAmount: Double,
    val targetAmount: Double,
    // (current / target).coerceIn(0f, 1f)
    val progressFraction: Float,
)

data class AccountBalanceItem(
    val displayName: String,
    val group: String,
    val balance: Double,
)

data class TxGroupSummary(
    val identifier: String,
    val displayName: String,
    val totalSpend: Double,
)

data class TransactionPreviewItem(
    val id: String,
    val description: String,
    val amount: Double,
    val date: String,
    val accountName: String,
)
