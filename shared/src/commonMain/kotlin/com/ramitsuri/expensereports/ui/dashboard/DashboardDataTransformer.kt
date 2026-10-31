package com.ramitsuri.expensereports.ui.dashboard

import com.ramitsuri.expensereports.model.CurrentBalance
import com.ramitsuri.expensereports.model.MonthYear
import com.ramitsuri.expensereports.model.Report
import com.ramitsuri.expensereports.model.Transaction
import com.ramitsuri.expensereports.model.toList
import kotlinx.datetime.Month

object DashboardDataTransformer {
    fun transform(
        currentMonthYear: MonthYear,
        currentBalances: List<CurrentBalance>,
        incomeWithoutGainsReport: Report?,
        afterDeductionExpensesReport: Report?,
        savingsRateReport: Report?,
        transactions: List<Transaction>,
        annualTargets: Map<String, Double> = emptyMap(),
        netWorthReport: Report? = null,
    ): DashboardUiState {
        // 1. Working Capital
        val cashBalances = currentBalances.filter { it.groupName.equals("Cash", ignoreCase = true) }
        val creditBalances = currentBalances.filter { it.groupName.equals("Credit Cards", ignoreCase = true) }

        val totalCash = cashBalances.sumOf { it.balance }.toDouble()
        val totalCreditDebt = creditBalances.sumOf { it.balance.abs() }.toDouble()
        val netLiquidity = totalCash - totalCreditDebt

        val accounts =
            (cashBalances + creditBalances)
                .filter { it.balance.toDouble() != 0.0 }
                .map {
                    AccountBalanceItem(
                        displayName = it.name,
                        group = it.groupName,
                        balance = it.balance.toDouble(),
                    )
                }
        val workingCapital =
            WorkingCapitalSummary(
                totalCash = totalCash,
                totalCreditDebt = totalCreditDebt,
                netLiquidity = netLiquidity,
                accounts = accounts,
            )

        // 2. Cash Flow & Multi-Month Trends
        val incomeAccount = incomeWithoutGainsReport?.accounts?.firstOrNull()
        val livingExpensesAccount = afterDeductionExpensesReport?.accounts?.firstOrNull()

        val incomeWithoutGains = incomeAccount?.monthTotals?.get(currentMonthYear)?.toDouble() ?: 0.0
        val livingExpenses = livingExpensesAccount?.monthTotals?.get(currentMonthYear)?.toDouble() ?: 0.0

        val incomeAcc =
            savingsRateReport?.accounts?.let { accounts ->
                accounts.firstOrNull { it.order == 0 }
                    ?: accounts.firstOrNull { it.name.contains("Income", ignoreCase = true) }
                    ?: accounts.getOrNull(0)
            }
        val taxAcc =
            savingsRateReport?.accounts?.let { accounts ->
                accounts.firstOrNull { it.order == 1 }
                    ?: accounts.firstOrNull { it.name.contains("Tax", ignoreCase = true) }
                    ?: accounts.getOrNull(1)
            }
        val expAcc =
            savingsRateReport?.accounts?.let { accounts ->
                accounts.firstOrNull { it.order == 2 }
                    ?: accounts.firstOrNull { it.name.contains("Expense", ignoreCase = true) }
                    ?: accounts.getOrNull(2)
            }

        val currentIncome = incomeAcc?.monthTotals?.get(currentMonthYear)?.toDouble() ?: incomeWithoutGains
        val currentTax = taxAcc?.monthTotals?.get(currentMonthYear)?.toDouble() ?: 0.0
        val afterTaxIncome = currentIncome - currentTax
        val netDelta = afterTaxIncome - livingExpenses

        fun computeMonthSavingsRatePercent(month: MonthYear): Double? {
            if (incomeAcc == null || taxAcc == null || expAcc == null) return null
            val inc = incomeAcc.monthTotals[month]?.toDouble() ?: 0.0
            val tax = taxAcc.monthTotals[month]?.toDouble() ?: 0.0
            val exp = expAcc.monthTotals[month]?.toDouble() ?: 0.0
            val afterTax = inc - tax
            return if (afterTax > 0.0) {
                val savings = afterTax - exp
                (savings / afterTax) * 100.0
            } else {
                0.0
            }
        }

        fun computeYtdSavingsRatePercent(): Double? {
            if (incomeAcc == null || taxAcc == null || expAcc == null) return null
            val ytdMonths = (currentMonthYear.copy(month = Month.JANUARY)..currentMonthYear).toList()
            val ytdIncome = ytdMonths.sumOf { incomeAcc.monthTotals[it]?.toDouble() ?: 0.0 }
            val ytdTax = ytdMonths.sumOf { taxAcc.monthTotals[it]?.toDouble() ?: 0.0 }
            val ytdExp = ytdMonths.sumOf { expAcc.monthTotals[it]?.toDouble() ?: 0.0 }

            val afterTax = ytdIncome - ytdTax
            return if (afterTax > 0.0) {
                val savings = afterTax - ytdExp
                (savings / afterTax) * 100.0
            } else {
                0.0
            }
        }

        val ytdSavingsRatePercent = computeYtdSavingsRatePercent()

        // Historical months in payload
        val allMonths =
            buildSet {
                incomeAccount?.monthTotals?.keys?.let { addAll(it) }
                livingExpensesAccount?.monthTotals?.keys?.let { addAll(it) }
                savingsRateReport?.accounts?.forEach { addAll(it.monthTotals.keys) }
            }.filter { it <= currentMonthYear }
                .sorted()

        val trailingMonths = allMonths.takeLast(6)

        val trailingNetDeltas =
            trailingMonths.map { month ->
                val inc =
                    incomeAcc?.monthTotals?.get(month)?.toDouble()
                        ?: (incomeAccount?.monthTotals?.get(month)?.toDouble() ?: 0.0)
                val tax = taxAcc?.monthTotals?.get(month)?.toDouble() ?: 0.0
                val exp =
                    livingExpensesAccount?.monthTotals?.get(month)?.toDouble()
                        ?: (expAcc?.monthTotals?.get(month)?.toDouble() ?: 0.0)
                TrendPoint(
                    periodLabel = month.toShortLabel(),
                    value = (inc - tax) - exp,
                )
            }

        val savingsRateMonths = allMonths.takeLast(36)

        val trailingSavingsRates =
            savingsRateMonths.map { month ->
                val fullLabel =
                    "${month.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${month.year}"
                TrendPoint(
                    periodLabel = month.toShortLabel(),
                    value = computeMonthSavingsRatePercent(month) ?: 0.0,
                    fullLabel = fullLabel,
                    subLabel = "'${month.year % 100}",
                )
            }

        val cashFlow =
            CashFlowSummary(
                incomeWithoutGains = incomeWithoutGains,
                afterTaxIncome = afterTaxIncome,
                livingExpenses = livingExpenses,
                netDelta = netDelta,
                savingsRatePercent = ytdSavingsRatePercent,
                trailingNetDeltas = trailingNetDeltas,
                trailingSavingsRates = trailingSavingsRates,
            )

        // 3. Top Spending Categories & Trailing Baselines
        val topSpendingCategories =
            if (afterDeductionExpensesReport != null && afterDeductionExpensesReport.accounts.size > 1) {
                val rootAccount = afterDeductionExpensesReport.accounts.first()
                val rootColons = rootAccount.name.count { it == ':' }
                val allChildren = afterDeductionExpensesReport.accounts.drop(1)
                val directChildren =
                    allChildren.filter {
                        it.name.count { c -> c == ':' } == rootColons + 1
                    }
                val categoryAccounts =
                    directChildren
                        .ifEmpty { allChildren }
                        .filterNot {
                            it.name.equals(RENT_ACCOUNT_NAME, ignoreCase = true) ||
                                it.name.startsWith("$RENT_ACCOUNT_NAME:", ignoreCase = true)
                        }

                val pastMonths = allMonths.filter { it < currentMonthYear }.takeLast(3)

                categoryAccounts.map { account ->
                    val categoryName = account.name.split(":").last().trim()
                    val currentAmount = account.monthTotals[currentMonthYear]?.toDouble() ?: 0.0
                    val pastAmounts = pastMonths.mapNotNull { account.monthTotals[it]?.toDouble() }
                    val trailingAvg = if (pastAmounts.isNotEmpty()) pastAmounts.average() else null
                    val pct = if (livingExpenses > 0.0) (currentAmount / livingExpenses) * 100.0 else 0.0

                    CategorySpendItem(
                        categoryName = categoryName,
                        currentAmount = currentAmount,
                        trailingAverageAmount = trailingAvg,
                        percentageOfTotal = pct,
                    )
                }.sortedByDescending { it.currentAmount }
                    .take(5)
            } else {
                emptyList()
            }

        // 4. Annual Contributions (Retirement / HSA)
        val retirementBalances = currentBalances.filter { it.groupName.equals("Retirement", ignoreCase = true) }
        val annualContributions =
            retirementBalances.map { item ->
                val current = item.balance.abs().toDouble()
                val target =
                    annualTargets[item.name] ?: when {
                        item.name.contains("401", ignoreCase = true) -> 23500.0
                        item.name.contains("HSA", ignoreCase = true) -> 8550.0
                        item.name.contains("IRA", ignoreCase = true) -> 7000.0
                        else -> 23500.0
                    }
                val fraction = if (target > 0.0) (current / target).toFloat().coerceIn(0f, 1f) else 0f
                AnnualContributionProgress(
                    name = item.name,
                    currentAmount = current,
                    targetAmount = target,
                    progressFraction = fraction,
                )
            }

        // 5. Trip Groups & Recent Activity Previews
        val travelBalances = currentBalances.filter { it.groupName.equals("Travel", ignoreCase = true) }
        val activeTxGroups =
            travelBalances.map {
                TxGroupSummary(
                    identifier = it.name,
                    displayName = it.name,
                    totalSpend = it.balance.abs().toDouble(),
                )
            }

        val recentTransactionsPreview =
            transactions
                .sortedByDescending { it.date }
                .take(3)
                .map { tx ->
                    val toAccount = tx.toSplits().firstOrNull()?.accountName?.split(":")?.lastOrNull()
                    val fromAccount = tx.fromSplits().firstOrNull()?.accountName?.split(":")?.lastOrNull()
                    val accountName = toAccount ?: fromAccount ?: ""
                    TransactionPreviewItem(
                        id = tx.id,
                        description = tx.description,
                        amount = tx.total.toDouble(),
                        date = tx.date.toString(),
                        accountName = accountName,
                    )
                }

        val selectedPeriod =
            "${currentMonthYear.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${currentMonthYear.year}"

        val netWorthSummary = extractNetWorthSummary(netWorthReport, currentMonthYear)

        return DashboardUiState(
            selectedPeriod = selectedPeriod,
            netWorth = netWorthSummary,
            isSensitiveDataMasked = true,
            workingCapital = workingCapital,
            monthlyCashFlow = cashFlow,
            topSpendingCategories = topSpendingCategories,
            annualContributions = annualContributions,
            activeTxGroups = activeTxGroups,
            recentTransactionsPreview = recentTransactionsPreview,
            isLoading = false,
            error = null,
        )
    }

    fun extractNetWorthSummary(
        netWorthReport: Report?,
        currentMonthYear: MonthYear,
    ): NetWorthSummary {
        val account =
            netWorthReport?.accounts?.firstOrNull()
                ?: return NetWorthSummary(0.0, 0.0, 0.0, emptyList(), isAvailable = false)

        val sortedMonthYears = account.monthTotals.keys.sorted()
        if (sortedMonthYears.isEmpty()) {
            return NetWorthSummary(0.0, 0.0, 0.0, emptyList(), isAvailable = false)
        }

        val candidateIndex =
            sortedMonthYears.indexOf(currentMonthYear).takeIf { it >= 0 }
                ?: sortedMonthYears.indexOfLast { it <= currentMonthYear }.takeIf { it >= 0 }
                ?: sortedMonthYears.lastIndex
        val currentIndex = candidateIndex.coerceIn(0, sortedMonthYears.lastIndex)
        val currentMonth = sortedMonthYears[currentIndex]
        val currentVal = account.monthTotals[currentMonth]?.toDouble() ?: 0.0

        val (delta, deltaPercent) =
            if (currentIndex > 0) {
                val priorMonth = sortedMonthYears[currentIndex - 1]
                val priorVal = account.monthTotals[priorMonth]?.toDouble() ?: 0.0
                val diff = currentVal - priorVal
                val pct = if (priorVal != 0.0) (diff / kotlin.math.abs(priorVal)) * 100.0 else 0.0
                Pair(diff, pct)
            } else {
                Pair(0.0, 0.0)
            }

        val startIndex = (currentIndex - 11).coerceAtLeast(0)
        val trailingPoints =
            sortedMonthYears.subList(startIndex, currentIndex + 1).map { month ->
                TrendPoint(
                    periodLabel = month.toShortLabel(),
                    value = account.monthTotals[month]?.toDouble() ?: 0.0,
                )
            }

        return NetWorthSummary(
            currentNetWorth = currentVal,
            previousMonthDelta = delta,
            previousMonthDeltaPercent = deltaPercent,
            trailingHistory = trailingPoints,
            isAvailable = true,
        )
    }

    private fun MonthYear.toShortLabel(): String {
        return month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }
    }

    private const val RENT_ACCOUNT_NAME = "Expenses:Apartment Rent"
}
