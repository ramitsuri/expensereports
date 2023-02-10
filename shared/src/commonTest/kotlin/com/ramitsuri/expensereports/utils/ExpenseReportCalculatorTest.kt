package com.ramitsuri.expensereports.utils

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ramitsuri.expensereports.data.AccountTotal
import com.ramitsuri.expensereports.data.ExpenseReport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ExpenseReportCalculatorTest {

    private lateinit var calculator: ExpenseReportCalculator
    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        calculator = ExpenseReportCalculator(getInitialReport(), dispatcher)
    }

    /**
     *     |   Name    | Month 1 | Month 2 | Month 3 | Month 4 |  Total  |
     *     |-----------|---------|---------|---------|---------|---------|
     *     |   Total   |   22    |   25    |   28    |   31    |   106   |
     *     | Account 1 |    1    |    2    |    3    |    4    |    10   |
     *     | Account 2 |    7    |    8    |    9    |   10    |    34   |
     *     | Account 3 |   14    |   15    |   16    |   17    |    62   |
     */
    @Test
    fun testCalculateFull_ifNoChangesMadeToAccountAndMonthSelections() = runTest(dispatcher) {
        // Arrange
        // Act
        val result = calculator.calculate(
            by = ExpenseReportCalculator.By.FULL
        ) as ExpenseReportView.Full
        val accountTotals = result.accountTotals

        // Assert Account 1
        val account1 = accountTotals[0]
        assert(10, account1.total)

        // Assert Account 2
        val account2 = accountTotals[1]
        assert(34, account2.total)

        // Assert Account 3
        val account3 = accountTotals[2]
        assert(62, account3.total)

        // Assert Totals row
        val totalsAccount = result.total
        assertEquals(4, totalsAccount.monthAmounts.size)
        assert(22, totalsAccount.monthAmounts[1])
        assert(25, totalsAccount.monthAmounts[2])
        assert(28, totalsAccount.monthAmounts[3])
        assert(31, totalsAccount.monthAmounts[4])
        assert(106, totalsAccount.total)
    }

    /**
     *     |   Name    | Month 1 | Month 2 | Month 3 | Month 4 |  Total  |
     *     |-----------|---------|---------|---------|---------|---------|
     *     |   Total   |   22    |   25    |   28    |   31    |   106   |
     *     | Account 1 |    1    |    2    |    3    |    4    |    10   |
     *     | Account 2 |    7    |    8    |    9    |   10    |    34   |
     *     | Account 3 |   14    |   15    |   16    |   17    |    62   |
     */
    @Test
    fun testCalculateByAccount_ifNoChangesMadeToAccountAndMonthSelections() = runTest(dispatcher) {
        // Arrange
        // Act
        val result = (calculator.calculate(
            by = ExpenseReportCalculator.By.ACCOUNT
        ) as ExpenseReportView.ByAccount)

        val accountTotals = result.accountTotals

        // Assert Account 1
        val account1Total = accountTotals["Account1"]
        assert(10, account1Total)

        // Assert Account 2
        val account2Total = accountTotals["Account2"]
        assert(34, account2Total)

        // Assert Account 3
        val account3Total = accountTotals["Account3"]
        assert(62, account3Total)

        // Assert Totals row
        val totalsAccountTotal = result.total
        assert(106, totalsAccountTotal)
    }

    /**
     *     |   Name    | Month 1 | Month 2 | Month 3 | Month 4 |  Total  |
     *     |-----------|---------|---------|---------|---------|---------|
     *     |   Total   |   22    |   25    |   28    |   31    |   106   |
     *     | Account 1 |    1    |    2    |    3    |    4    |    10   |
     *     | Account 2 |    7    |    8    |    9    |   10    |    34   |
     *     | Account 3 |   14    |   15    |   16    |   17    |    62   |
     */
    @Test
    fun testCalculateByMonth_ifNoChangesMadeToAccountAndMonthSelections() = runTest(dispatcher) {
        // Arrange
        // Act
        val result = (calculator.calculate(
            by = ExpenseReportCalculator.By.MONTH
        ) as ExpenseReportView.ByMonth)

        val monthTotals = result.monthTotals

        // Assert Month 1
        val month1Total = monthTotals[1]
        assert(22, month1Total)

        // Assert Month 2
        val month2Total = monthTotals[2]
        assert(25, month2Total)

        // Assert Month 3
        val month3Total = monthTotals[3]
        assert(28, month3Total)

        // Assert Month 4
        val month4Total = monthTotals[4]
        assert(31, month4Total)

        // Assert Totals row
        val totalsAccountTotal = result.total
        assert(106, totalsAccountTotal)
    }

    /**
     *     |   Name    | Month 1 | Month 2 | Month 3 | Month 4 |  Total  |
     *     |-----------|---------|---------|---------|---------|---------|
     *     |   Total   |   21    |   23    |   25    |   27    |    96   |
     *     | Account 2 |    7    |    8    |    9    |   10    |    34   |
     *     | Account 3 |   14    |   15    |   16    |   17    |    62   |
     */
    @Test
    fun testCalculateFull_ifAccount1Removed() = runTest(dispatcher) {
        // Arrange
        // Act
        val result = calculator.calculate(
            by = ExpenseReportCalculator.By.FULL,
            selectedAccounts = listOf("Account2", "Account3")
        ) as ExpenseReportView.Full
        val accountTotals = result.accountTotals

        // Assert Account 2
        val account2 = accountTotals[0]
        assert(34, account2.total)

        // Assert Account 3
        val account3 = accountTotals[1]
        assert(62, account3.total)

        // Assert Totals row
        val totalsAccount = result.total
        assertEquals(4, totalsAccount.monthAmounts.size)
        assert(21, totalsAccount.monthAmounts[1])
        assert(23, totalsAccount.monthAmounts[2])
        assert(25, totalsAccount.monthAmounts[3])
        assert(27, totalsAccount.monthAmounts[4])
        assert(96, totalsAccount.total)
    }

    /**
     *     |   Name    | Month 1 | Month 2 | Month 3 | Month 4 |  Total  |
     *     |-----------|---------|---------|---------|---------|---------|
     *     |   Total   |    7    |    8    |    9    |   10    |    34   |
     *     | Account 2 |    7    |    8    |    9    |   10    |    34   |
     */
    @Test
    fun testCalculateFull_ifAccount1And3Removed() = runTest(dispatcher) {
        // Arrange
        // Act
        val result = calculator.calculate(
            by = ExpenseReportCalculator.By.FULL,
            selectedAccounts = listOf("Account2")
        ) as ExpenseReportView.Full
        val accountTotals = result.accountTotals

        // Assert Account 2
        val account2 = accountTotals[0]
        assert(34, account2.total)

        // Assert Totals row
        val totalsAccount = result.total
        assertEquals(4, totalsAccount.monthAmounts.size)
        assert(7, totalsAccount.monthAmounts[1])
        assert(8, totalsAccount.monthAmounts[2])
        assert(9, totalsAccount.monthAmounts[3])
        assert(10, totalsAccount.monthAmounts[4])
        assert(34, totalsAccount.total)
    }

    /**
     *     |   Name    | Month 2 | Month 3 |  Total  |
     *     |-----------|---------|---------|---------|
     *     |   Total   |   25    |   28    |    53   |
     *     | Account 1 |    2    |    3    |     5   |
     *     | Account 2 |    8    |    9    |    17   |
     *     | Account 3 |   15    |   16    |    31   |
     */
    @Test
    fun testCalculateFull_ifMonth1And4Removed() = runTest(dispatcher) {
        // Arrange
        // Act
        val result = calculator.calculate(
            by = ExpenseReportCalculator.By.FULL,
            selectedMonths = listOf(2, 3)
        ) as ExpenseReportView.Full
        val accountTotals = result.accountTotals

        // Assert Account 1
        val account1 = accountTotals[0]
        assert(5, account1.total)

        // Assert Account 2
        val account2 = accountTotals[1]
        assert(17, account2.total)

        // Assert Account 3
        val account3 = accountTotals[2]
        assert(31, account3.total)

        // Assert Totals row
        val totalsAccount = result.total
        assertEquals(2, totalsAccount.monthAmounts.size)
        assert(25, totalsAccount.monthAmounts[2])
        assert(28, totalsAccount.monthAmounts[3])
        assert(53, totalsAccount.total)
    }

    /**
     *     |   Name    | Month 1 | Month 2 | Month 3 |  Total  |
     *     |-----------|---------|---------|---------|---------|
     *     |   Total   |   22    |   25    |   28    |    75   |
     *     | Account 1 |    1    |    2    |    3    |     6   |
     *     | Account 2 |    7    |    8    |    9    |    24   |
     *     | Account 3 |   14    |   15    |   16    |    45   |
     */
    @Test
    fun testCalculateFull_ifMonth4Removed() = runTest(dispatcher) {
        // Arrange
        // Act
        val result = calculator.calculate(
            by = ExpenseReportCalculator.By.FULL,
            selectedMonths = listOf(1, 2, 3)
        ) as ExpenseReportView.Full
        val accountTotals = result.accountTotals

        // Assert Account 1
        val account1 = accountTotals[0]
        assert(6, account1.total)

        // Assert Account 2
        val account2 = accountTotals[1]
        assert(24, account2.total)

        // Assert Account 3
        val account3 = accountTotals[2]
        assert(45, account3.total)

        // Assert Totals row
        val totalsAccount = result.total
        assertEquals(3, totalsAccount.monthAmounts.size)
        assert(22, totalsAccount.monthAmounts[1])
        assert(25, totalsAccount.monthAmounts[2])
        assert(28, totalsAccount.monthAmounts[3])
        assert(75, totalsAccount.total)
    }

    /**
     *     |   Name    | Month 1 | Month 3 | Month 4 |  Total  |
     *     |-----------|---------|---------|---------|---------|
     *     |   Total   |   15    |   19    |   21    |    55   |
     *     | Account 1 |    1    |    3    |    4    |     8   |
     *     | Account 3 |   14    |   16    |   17    |    47   |
     */
    @Test
    fun testCalculateFull_ifMonth2AndAccount2Removed() = runTest(dispatcher) {
        // Arrange
        // Act
        val result = calculator.calculate(
            by = ExpenseReportCalculator.By.FULL,
            selectedAccounts = listOf("Account1", "Account3"),
            selectedMonths = listOf(1, 3, 4)
        ) as ExpenseReportView.Full
        val accountTotals = result.accountTotals

        // Assert Account 1
        val account1 = accountTotals[0]
        assert(8, account1.total)

        // Assert Account 3
        val account3 = accountTotals[1]
        assert(47, account3.total)

        // Assert Totals row
        val totalsAccount = result.total
        assertEquals(3, totalsAccount.monthAmounts.size)
        assert(15, totalsAccount.monthAmounts[1])
        assert(19, totalsAccount.monthAmounts[3])
        assert(21, totalsAccount.monthAmounts[4])
        assert(55, totalsAccount.total)
    }

    /**
     *     |   Name    | Month 1 | Month 3 | Month 4 |  Total  |
     *     |-----------|---------|---------|---------|---------|
     *     |   Total   |   15    |   19    |   21    |    55   |
     *     | Account 1 |    1    |    3    |    4    |     8   |
     *     | Account 3 |   14    |   16    |   17    |    47   |
     */
    @Test
    fun testCalculateByAccounts_ifMonth2AndAccount2Removed() = runTest(dispatcher) {
        // Arrange
        // Act
        val result = (calculator.calculate(
            by = ExpenseReportCalculator.By.ACCOUNT,
            selectedAccounts = listOf("Account1", "Account3"),
            selectedMonths = listOf(1, 3, 4)
        ) as ExpenseReportView.ByAccount)

        val accountTotals = result.accountTotals

        // Assert Account 1
        val account1Amount = accountTotals["Account1"]
        assert(8, account1Amount)

        // Assert Account 3
        val account3Amount = accountTotals["Account3"]
        assert(47, account3Amount)

        // Assert Totals row
        val totalsAccountAmount = result.total
        assert(55, totalsAccountAmount)
    }

    /**
     *     |   Name    | Month 1 | Month 3 | Month 4 |  Total  |
     *     |-----------|---------|---------|---------|---------|
     *     |   Total   |   15    |   19    |   21    |    55   |
     *     | Account 1 |    1    |    3    |    4    |     8   |
     *     | Account 3 |   14    |   16    |   17    |    47   |
     */
    @Test
    fun testCalculateByMonth_ifMonth2AndAccount2Removed() = runTest(dispatcher) {
        // Arrange
        // Act
        val result = (calculator.calculate(
            by = ExpenseReportCalculator.By.MONTH,
            selectedAccounts = listOf("Account1", "Account3"),
            selectedMonths = listOf(1, 3, 4)
        ) as ExpenseReportView.ByMonth)

        val monthTotals = result.monthTotals

        // Assert Month 1
        val month1Total = monthTotals[1]
        assert(15, month1Total)

        // Assert Month 3
        val month3Total = monthTotals[3]
        assert(19, month3Total)

        // Assert Month 3
        val month4Total = monthTotals[4]
        assert(21, month4Total)

        // Assert Totals row
        val totalsAccountTotal = result.total
        assert(55, totalsAccountTotal)
    }

    private fun assert(expectedAmount: Int, actualAmount: BigDecimal?) {
        assertEquals(BigDecimal.fromInt(expectedAmount), actualAmount!!)
    }

    /**
     * Returns a report that looks like
     *
     *     |   Name    | Month 1 | Month 2 | Month 3 | Month 4 |
     *     |-----------|---------|---------|---------|---------|
     *     | Account 1 |    1    |    2    |    3    |    4    |
     *     | Account 2 |    7    |    8    |    9    |   10    |
     *     | Account 3 |   14    |   15    |   16    |   17    |
     */
    private fun getInitialReport(): ExpenseReport {
        return ExpenseReport(
            name = "Report",
            accountTotals = listOf(
                AccountTotal(
                    name = "Account1",
                    children = listOf(),
                    monthAmounts = mapOf(
                        1 to BigDecimal.parseString("1"),
                        2 to BigDecimal.parseString("2"),
                        3 to BigDecimal.parseString("3"),
                        4 to BigDecimal.parseString("4")
                    )
                ),
                AccountTotal(
                    name = "Account2",
                    children = listOf(),
                    monthAmounts = mapOf(
                        1 to BigDecimal.parseString("7"),
                        2 to BigDecimal.parseString("8"),
                        3 to BigDecimal.parseString("9"),
                        4 to BigDecimal.parseString("10")

                    )
                ),
                AccountTotal(
                    name = "Account3",
                    children = listOf(),
                    monthAmounts = mapOf(
                        1 to BigDecimal.parseString("14"),
                        2 to BigDecimal.parseString("15"),
                        3 to BigDecimal.parseString("16"),
                        4 to BigDecimal.parseString("17")
                    )
                )
            )
        )
    }
}