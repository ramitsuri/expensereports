package com.ramitsuri.expensereports.ui.dashboard

import com.ramitsuri.expensereports.model.CurrentBalance
import com.ramitsuri.expensereports.model.MonthYear
import com.ramitsuri.expensereports.model.Report
import com.ramitsuri.expensereports.model.Transaction
import com.ramitsuri.expensereports.model.TransactionSplit
import com.ramitsuri.expensereports.model.toList
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import org.junit.Test
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DashboardDataTransformerTest {
    private val oct2026 = MonthYear(Month.OCTOBER, 2026)
    private val sep2026 = MonthYear(Month.SEPTEMBER, 2026)
    private val aug2026 = MonthYear(Month.AUGUST, 2026)
    private val jul2026 = MonthYear(Month.JULY, 2026)
    private val jun2026 = MonthYear(Month.JUNE, 2026)
    private val may2026 = MonthYear(Month.MAY, 2026)

    @Test
    fun testWorkingCapitalCalculation() {
        val balances =
            listOf(
                CurrentBalance(name = "Checking", balance = BigDecimal("10000.00"), groupName = "Cash"),
                CurrentBalance(name = "Savings", balance = BigDecimal("2500.00"), groupName = "Cash"),
                CurrentBalance(name = "Zero Cash", balance = BigDecimal("0.00"), groupName = "Cash"),
                CurrentBalance(name = "Chase Sapphire", balance = BigDecimal("-1500.50"), groupName = "Credit Cards"),
                CurrentBalance(name = "Amex Gold", balance = BigDecimal("-499.50"), groupName = "Credit Cards"),
                CurrentBalance(name = "Zero Card", balance = BigDecimal("0.00"), groupName = "Credit Cards"),
                CurrentBalance(name = "Brokerage", balance = BigDecimal("50000.00"), groupName = "Investments"),
            )

        val state =
            DashboardDataTransformer.transform(
                currentMonthYear = oct2026,
                currentBalances = balances,
                incomeWithoutGainsReport = null,
                afterDeductionExpensesReport = null,
                savingsRateReport = null,
                transactions = emptyList(),
            )

        assertEquals(12500.0, state.workingCapital.totalCash, 0.01)
        assertEquals(2000.0, state.workingCapital.totalCreditDebt, 0.01)
        assertEquals(10500.0, state.workingCapital.netLiquidity, 0.01)
        assertEquals(4, state.workingCapital.accounts.size)
    }

    @Test
    fun testCashFlowAndTrailingTrends() {
        val months = listOf(may2026, jun2026, jul2026, aug2026, sep2026, oct2026)

        val incomeReport =
            Report(
                name = "Income Without Gains",
                withCumulativeBalance = false,
                accounts =
                    listOf(
                        Report.Account(
                            name = "Income",
                            order = 0,
                            monthTotals =
                                months.associateWith { month ->
                                    if (month == oct2026) BigDecimal("8000.00") else BigDecimal("7500.00")
                                },
                        ),
                    ),
            )

        val expensesReport =
            Report(
                name = "After Deduction Expenses",
                withCumulativeBalance = false,
                accounts =
                    listOf(
                        Report.Account(
                            name = "Expenses",
                            order = 0,
                            monthTotals =
                                months.associateWith { month ->
                                    if (month == oct2026) BigDecimal("5000.00") else BigDecimal("4500.00")
                                },
                        ),
                    ),
            )

        val savingsRateReport =
            Report(
                name = "Savings Rate",
                withCumulativeBalance = false,
                accounts =
                    listOf(
                        Report.Account(
                            name = "Income",
                            order = 0,
                            monthTotals = months.associateWith { BigDecimal("8000.00") },
                        ),
                        Report.Account(
                            name = "Taxes",
                            order = 1,
                            monthTotals = months.associateWith { BigDecimal("2000.00") },
                        ),
                        Report.Account(
                            name = "Expenses",
                            order = 2,
                            monthTotals = months.associateWith { BigDecimal("4000.00") },
                        ),
                    ),
            )

        val state =
            DashboardDataTransformer.transform(
                currentMonthYear = oct2026,
                currentBalances = emptyList(),
                incomeWithoutGainsReport = incomeReport,
                afterDeductionExpensesReport = expensesReport,
                savingsRateReport = savingsRateReport,
                transactions = emptyList(),
            )

        assertEquals("October 2026", state.selectedPeriod)
        assertEquals(8000.0, state.monthlyCashFlow.incomeWithoutGains, 0.01)
        assertEquals(6000.0, state.monthlyCashFlow.afterTaxIncome, 0.01)
        assertEquals(5000.0, state.monthlyCashFlow.livingExpenses, 0.01)
        // After-tax cash flow = 6000 - 5000 = 1000
        assertEquals(1000.0, state.monthlyCashFlow.netDelta, 0.01)

        // After tax income = 8000 - 2000 = 6000. Savings = 6000 - 4000 = 2000. Rate = 2000/6000 = 33.333%
        assertNotNull(state.monthlyCashFlow.savingsRatePercent)
        assertEquals(33.33, state.monthlyCashFlow.savingsRatePercent, 0.1)

        // Trailing net deltas should have 6 items
        assertEquals(6, state.monthlyCashFlow.trailingNetDeltas.size)
        assertEquals("Oct", state.monthlyCashFlow.trailingNetDeltas.last().periodLabel)
        assertEquals(1000.0, state.monthlyCashFlow.trailingNetDeltas.last().value, 0.01)
        assertEquals(1500.0, state.monthlyCashFlow.trailingNetDeltas.first().value, 0.01)

        // Trailing savings rates
        assertEquals(6, state.monthlyCashFlow.trailingSavingsRates.size)
        assertEquals(33.33, state.monthlyCashFlow.trailingSavingsRates.last().value, 0.1)
        assertEquals("October 2026", state.monthlyCashFlow.trailingSavingsRates.last().fullLabel)
        assertEquals("'26", state.monthlyCashFlow.trailingSavingsRates.last().subLabel)
    }

    @Test
    fun testTrailingSavingsRatesUpToThreeYears() {
        val startMonth = MonthYear(Month.JANUARY, 2023)
        val allMonths = (startMonth..oct2026).toList()

        val incomeMap = allMonths.associateWith { BigDecimal("10000.00") }
        val taxMap = allMonths.associateWith { BigDecimal("2000.00") }
        val expenseMap = allMonths.associateWith { BigDecimal("4000.00") }

        val savingsRateReport =
            Report(
                name = "Savings Rate",
                withCumulativeBalance = false,
                accounts =
                    listOf(
                        Report.Account(name = "Income", order = 0, monthTotals = incomeMap),
                        Report.Account(name = "Taxes", order = 1, monthTotals = taxMap),
                        Report.Account(name = "Expenses", order = 2, monthTotals = expenseMap),
                    ),
            )

        val state =
            DashboardDataTransformer.transform(
                currentMonthYear = oct2026,
                currentBalances = emptyList(),
                incomeWithoutGainsReport = null,
                afterDeductionExpensesReport = null,
                savingsRateReport = savingsRateReport,
                transactions = emptyList(),
            )

        // Should cap at 36 months (3 years)
        assertEquals(36, state.monthlyCashFlow.trailingSavingsRates.size)
        assertEquals("Oct", state.monthlyCashFlow.trailingSavingsRates.last().periodLabel)
        assertEquals("'26", state.monthlyCashFlow.trailingSavingsRates.last().subLabel)
        assertEquals("October 2026", state.monthlyCashFlow.trailingSavingsRates.last().fullLabel)
        assertEquals(50.0, state.monthlyCashFlow.trailingSavingsRates.last().value, 0.01)
    }

    @Test
    fun testTopSpendingCategoriesAndTrailingBaselines() {
        val expensesReport =
            Report(
                name = "After Deduction Expenses",
                withCumulativeBalance = false,
                accounts =
                    listOf(
                        Report.Account(
                            name = "Expenses",
                            order = 0,
                            monthTotals =
                                mapOf(
                                    jul2026 to BigDecimal("4000.00"),
                                    aug2026 to BigDecimal("4200.00"),
                                    sep2026 to BigDecimal("4500.00"),
                                    oct2026 to BigDecimal("5000.00"),
                                ),
                        ),
                        Report.Account(
                            name = "Expenses:Apartment Rent",
                            order = 1,
                            monthTotals =
                                mapOf(
                                    jul2026 to BigDecimal("2000.00"),
                                    aug2026 to BigDecimal("2000.00"),
                                    sep2026 to BigDecimal("2000.00"),
                                    oct2026 to BigDecimal("2000.00"),
                                ),
                        ),
                        Report.Account(
                            name = "Expenses:Groceries",
                            order = 2,
                            monthTotals =
                                mapOf(
                                    jul2026 to BigDecimal("800.00"),
                                    aug2026 to BigDecimal("900.00"),
                                    sep2026 to BigDecimal("1000.00"),
                                    oct2026 to BigDecimal("1200.00"),
                                ),
                        ),
                        Report.Account(
                            name = "Expenses:Dining",
                            order = 3,
                            monthTotals =
                                mapOf(
                                    jul2026 to BigDecimal("400.00"),
                                    aug2026 to BigDecimal("500.00"),
                                    sep2026 to BigDecimal("600.00"),
                                    oct2026 to BigDecimal("800.00"),
                                ),
                        ),
                        Report.Account(
                            name = "Expenses:Utilities",
                            order = 4,
                            monthTotals =
                                mapOf(
                                    jul2026 to BigDecimal("300.00"),
                                    aug2026 to BigDecimal("350.00"),
                                    sep2026 to BigDecimal("350.00"),
                                    oct2026 to BigDecimal("400.00"),
                                ),
                        ),
                        Report.Account(
                            name = "Expenses:Transportation",
                            order = 5,
                            monthTotals =
                                mapOf(
                                    jul2026 to BigDecimal("200.00"),
                                    aug2026 to BigDecimal("250.00"),
                                    sep2026 to BigDecimal("300.00"),
                                    oct2026 to BigDecimal("350.00"),
                                ),
                        ),
                        Report.Account(
                            name = "Expenses:Shopping",
                            order = 6,
                            monthTotals =
                                mapOf(
                                    jul2026 to BigDecimal("300.00"),
                                    aug2026 to BigDecimal("200.00"),
                                    sep2026 to BigDecimal("250.00"),
                                    oct2026 to BigDecimal("250.00"),
                                ),
                        ),
                    ),
            )

        val state =
            DashboardDataTransformer.transform(
                currentMonthYear = oct2026,
                currentBalances = emptyList(),
                incomeWithoutGainsReport = null,
                afterDeductionExpensesReport = expensesReport,
                savingsRateReport = null,
                transactions = emptyList(),
            )

        // Rent is excluded so 5 categories remain
        assertEquals(5, state.topSpendingCategories.size)
        assertTrue(state.topSpendingCategories.none { it.categoryName.contains("Rent", ignoreCase = true) })

        val groceries = state.topSpendingCategories[0]
        assertEquals("Groceries", groceries.categoryName)
        assertEquals(1200.0, groceries.currentAmount, 0.01)
        // Average of Jul (800), Aug (900), Sep (1000) = 900.0
        assertEquals(900.0, groceries.trailingAverageAmount!!, 0.01)
        assertEquals(24.0, groceries.percentageOfTotal, 0.01)

        val dining = state.topSpendingCategories[1]
        assertEquals("Dining", dining.categoryName)
        assertEquals(800.0, dining.currentAmount, 0.01)

        val shopping = state.topSpendingCategories[4]
        assertEquals("Shopping", shopping.categoryName)
        assertEquals(250.0, shopping.currentAmount, 0.01)
    }

    @Test
    fun testAnnualContributions() {
        val balances =
            listOf(
                CurrentBalance(name = "Ramit 401(k)", balance = BigDecimal("14200.00"), groupName = "Retirement"),
                CurrentBalance(name = "Jess 401(k)", balance = BigDecimal("23500.00"), groupName = "Retirement"),
                CurrentBalance(name = "Family HSA", balance = BigDecimal("5130.00"), groupName = "Retirement"),
            )

        val state =
            DashboardDataTransformer.transform(
                currentMonthYear = oct2026,
                currentBalances = balances,
                incomeWithoutGainsReport = null,
                afterDeductionExpensesReport = null,
                savingsRateReport = null,
                transactions = emptyList(),
            )

        assertEquals(3, state.annualContributions.size)
        val ramit401k = state.annualContributions[0]
        assertEquals("Ramit 401(k)", ramit401k.name)
        assertEquals(14200.0, ramit401k.currentAmount, 0.01)
        assertEquals(23500.0, ramit401k.targetAmount, 0.01)
        assertEquals(0.604f, ramit401k.progressFraction, 0.01f)

        val jess401k = state.annualContributions[1]
        assertEquals(1.0f, jess401k.progressFraction, 0.01f)

        val hsa = state.annualContributions[2]
        assertEquals(8550.0, hsa.targetAmount, 0.01)
        assertEquals(0.6f, hsa.progressFraction, 0.01f)
    }

    @Test
    fun testTripsAndRecentTransactions() {
        val balances =
            listOf(
                CurrentBalance(name = "ITA2025", balance = BigDecimal("-3450.00"), groupName = "Travel"),
                CurrentBalance(name = "PR2025", balance = BigDecimal("-1200.00"), groupName = "Travel"),
            )

        val txs =
            listOf(
                Transaction(
                    id = "1",
                    date = LocalDate(2026, 10, 28),
                    description = "Trader Joe's",
                    splits =
                        listOf(
                            TransactionSplit("Liabilities:Credit Card", BigDecimal("-85.50")),
                            TransactionSplit("Expenses:Groceries", BigDecimal("85.50")),
                        ),
                ),
                Transaction(
                    id = "2",
                    date = LocalDate(2026, 10, 27),
                    description = "Shell Gas",
                    splits =
                        listOf(
                            TransactionSplit("Liabilities:Credit Card", BigDecimal("-45.00")),
                            TransactionSplit("Expenses:Auto:Gas", BigDecimal("45.00")),
                        ),
                ),
                Transaction(
                    id = "3",
                    date = LocalDate(2026, 10, 25),
                    description = "Chipotle",
                    splits =
                        listOf(
                            TransactionSplit("Liabilities:Credit Card", BigDecimal("-24.50")),
                            TransactionSplit("Expenses:Dining", BigDecimal("24.50")),
                        ),
                ),
                Transaction(
                    id = "4",
                    date = LocalDate(2026, 10, 20),
                    description = "Older Tx",
                    splits =
                        listOf(
                            TransactionSplit("Liabilities:Credit Card", BigDecimal("-10.00")),
                            TransactionSplit("Expenses:Misc", BigDecimal("10.00")),
                        ),
                ),
            )

        val state =
            DashboardDataTransformer.transform(
                currentMonthYear = oct2026,
                currentBalances = balances,
                incomeWithoutGainsReport = null,
                afterDeductionExpensesReport = null,
                savingsRateReport = null,
                transactions = txs,
            )

        assertEquals(2, state.activeTxGroups.size)
        assertEquals("ITA2025", state.activeTxGroups[0].identifier)
        assertEquals(3450.0, state.activeTxGroups[0].totalSpend, 0.01)

        assertEquals(3, state.recentTransactionsPreview.size)
        assertEquals("Trader Joe's", state.recentTransactionsPreview[0].description)
        assertEquals("Groceries", state.recentTransactionsPreview[0].accountName)
        assertEquals(85.50, state.recentTransactionsPreview[0].amount, 0.01)
    }

    @Test
    fun testEmptyAndGracefulHandling() {
        val state =
            DashboardDataTransformer.transform(
                currentMonthYear = oct2026,
                currentBalances = emptyList(),
                incomeWithoutGainsReport = null,
                afterDeductionExpensesReport = null,
                savingsRateReport = null,
                transactions = emptyList(),
            )

        assertEquals(0.0, state.workingCapital.netLiquidity, 0.01)
        assertEquals(0.0, state.monthlyCashFlow.netDelta, 0.01)
        assertNull(state.monthlyCashFlow.savingsRatePercent)
        assertTrue(state.monthlyCashFlow.trailingNetDeltas.isEmpty())
        assertTrue(state.topSpendingCategories.isEmpty())
        assertTrue(state.annualContributions.isEmpty())
        assertTrue(state.activeTxGroups.isEmpty())
        assertTrue(state.recentTransactionsPreview.isEmpty())
    }

    @Test
    fun testYtdSavingsRateWhenCurrentMonthHasNotPaidSalaryYet() {
        // From Jan to Sep: Income $10,000, Taxes $2,000, Expenses $4,000 per month
        // In Oct (current month): Income $0 (no salary yet), Taxes $0, Expenses $2,000
        val jan2026 = MonthYear(Month.JANUARY, 2026)
        val monthsJanToSep = (jan2026..sep2026).toList()

        val incomeMap = monthsJanToSep.associateWith { BigDecimal("10000.00") }.toMutableMap()
        incomeMap[oct2026] = BigDecimal("0.00")

        val taxMap = monthsJanToSep.associateWith { BigDecimal("2000.00") }.toMutableMap()
        taxMap[oct2026] = BigDecimal("0.00")

        val expenseMap = monthsJanToSep.associateWith { BigDecimal("4000.00") }.toMutableMap()
        expenseMap[oct2026] = BigDecimal("2000.00")

        val savingsRateReport =
            Report(
                name = "Savings Rate",
                withCumulativeBalance = false,
                accounts =
                    listOf(
                        Report.Account(name = "Income", order = 0, monthTotals = incomeMap),
                        Report.Account(name = "Taxes", order = 1, monthTotals = taxMap),
                        Report.Account(name = "Expenses", order = 2, monthTotals = expenseMap),
                    ),
            )

        val state =
            DashboardDataTransformer.transform(
                currentMonthYear = oct2026,
                currentBalances = emptyList(),
                incomeWithoutGainsReport = null,
                afterDeductionExpensesReport = null,
                savingsRateReport = savingsRateReport,
                transactions = emptyList(),
            )

        // YTD:
        // Income = 9 * 10,000 = $90,000
        // Taxes = 9 * 2,000 = $18,000
        // After-tax = $72,000
        // Expenses = 9 * 4,000 + 2,000 = $38,000
        // Savings = 72,000 - 38,000 = $34,000
        // YTD Savings Rate = 34,000 / 72,000 = 47.22%
        assertNotNull(state.monthlyCashFlow.savingsRatePercent)
        assertEquals(47.22, state.monthlyCashFlow.savingsRatePercent, 0.1)

        // But trailing monthly savings rate for October specifically would be 0.0 (or negative)
        val octTrailing = state.monthlyCashFlow.trailingSavingsRates.first { it.periodLabel == "Oct" }
        assertEquals(0.0, octTrailing.value, 0.01)
    }

    @Test
    fun testNetWorthSummaryExtraction_withTrailingHistory() {
        // Create 14 months of history: Sep 2025 through Oct 2026
        val months = (MonthYear(Month.SEPTEMBER, 2025)..oct2026).toList()
        val totals =
            months.mapIndexed { index, my ->
                my to BigDecimal((100000 + index * 5000).toString())
            }.toMap()

        val netWorthReport =
            Report(
                name = "NetWorth",
                withCumulativeBalance = true,
                accounts =
                    listOf(
                        Report.Account(name = "Net Worth", order = 0, monthTotals = totals),
                    ),
            )

        val summary = DashboardDataTransformer.extractNetWorthSummary(netWorthReport, oct2026)

        assertTrue(summary.isAvailable)
        // Sep 2025 is index 0: 100,000. Oct 2026 is index 13: 165,000. Sep 2026 is index 12: 160,000.
        assertEquals(165000.0, summary.currentNetWorth, 0.01)
        assertEquals(5000.0, summary.previousMonthDelta, 0.01)
        // 5000 / 160000 * 100 = 3.125%
        assertEquals(3.125, summary.previousMonthDeltaPercent, 0.001)

        // Trailing points: up to 12 months (Nov 2025 to Oct 2026)
        assertEquals(12, summary.trailingHistory.size)
        assertEquals("Oct", summary.trailingHistory.last().periodLabel)
        assertEquals(165000.0, summary.trailingHistory.last().value, 0.01)
        assertEquals("Nov", summary.trailingHistory.first().periodLabel)
        assertEquals(110000.0, summary.trailingHistory.first().value, 0.01)
    }

    @Test
    fun testNetWorthSummaryExtraction_singleMonth() {
        val netWorthReport =
            Report(
                name = "NetWorth",
                withCumulativeBalance = true,
                accounts =
                    listOf(
                        Report.Account(
                            name = "Net Worth",
                            order = 0,
                            monthTotals = mapOf(oct2026 to BigDecimal("50000.00")),
                        ),
                    ),
            )

        val summary = DashboardDataTransformer.extractNetWorthSummary(netWorthReport, oct2026)

        assertTrue(summary.isAvailable)
        assertEquals(50000.0, summary.currentNetWorth, 0.01)
        assertEquals(0.0, summary.previousMonthDelta, 0.01)
        assertEquals(0.0, summary.previousMonthDeltaPercent, 0.01)
        assertEquals(1, summary.trailingHistory.size)
        assertEquals("Oct", summary.trailingHistory.first().periodLabel)
        assertEquals(50000.0, summary.trailingHistory.first().value, 0.01)
    }

    @Test
    fun testNetWorthSummaryExtraction_nullOrEmptyReport() {
        val nullSummary = DashboardDataTransformer.extractNetWorthSummary(null, oct2026)
        assertEquals(false, nullSummary.isAvailable)
        assertEquals(0.0, nullSummary.currentNetWorth, 0.01)
        assertTrue(nullSummary.trailingHistory.isEmpty())

        val emptyAccountReport =
            Report(
                name = "NetWorth",
                withCumulativeBalance = true,
                accounts = emptyList(),
            )
        val emptySummary = DashboardDataTransformer.extractNetWorthSummary(emptyAccountReport, oct2026)
        assertEquals(false, emptySummary.isAvailable)
        assertEquals(0.0, emptySummary.currentNetWorth, 0.01)
        assertTrue(emptySummary.trailingHistory.isEmpty())
    }

    @Test
    fun testNetWorthSummaryExtraction_fallbackToLatestWhenMonthNotInReport() {
        val totals =
            mapOf(
                aug2026 to BigDecimal("100000.00"),
                sep2026 to BigDecimal("105000.00"),
            )
        val netWorthReport =
            Report(
                name = "NetWorth",
                withCumulativeBalance = true,
                accounts =
                    listOf(
                        Report.Account(name = "Net Worth", order = 0, monthTotals = totals),
                    ),
            )

        // Requesting oct2026 which is not in totals; should fallback to sep2026 (index 1)
        val summary = DashboardDataTransformer.extractNetWorthSummary(netWorthReport, oct2026)

        assertTrue(summary.isAvailable)
        assertEquals(105000.0, summary.currentNetWorth, 0.01)
        assertEquals(5000.0, summary.previousMonthDelta, 0.01)
        assertEquals(2, summary.trailingHistory.size)
    }
}
