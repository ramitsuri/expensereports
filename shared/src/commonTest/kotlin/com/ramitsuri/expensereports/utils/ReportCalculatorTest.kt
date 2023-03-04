package com.ramitsuri.expensereports.utils

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ramitsuri.expensereports.data.AccountTotalWithTotal
import com.ramitsuri.expensereports.data.Report
import com.ramitsuri.expensereports.data.ReportType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.Clock
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ReportCalculatorTest {

    private lateinit var calculator: ReportCalculator
    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
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
        setupSimpleCalculator()

        // Act
        val result = calculator.calculate(
            by = ReportCalculator.By.FULL
        ) as ReportView.Full
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
        setupSimpleCalculator()

        // Act
        val result = (calculator.calculate(
            by = ReportCalculator.By.ACCOUNT
        ) as ReportView.ByAccount)

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
        setupSimpleCalculator()

        // Act
        val result = (calculator.calculate(
            by = ReportCalculator.By.MONTH
        ) as ReportView.ByMonth)

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
    fun testCalculateFull_ifAccount1NotSelected() = runTest(dispatcher) {
        // Arrange
        setupSimpleCalculator()

        // Act
        val result = calculator.calculate(
            by = ReportCalculator.By.FULL,
            selectedAccounts = listOf("Account2", "Account3")
        ) as ReportView.Full
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
    fun testCalculateFull_ifAccount1And3NotSelected() = runTest(dispatcher) {
        // Arrange
        setupSimpleCalculator()

        // Act
        val result = calculator.calculate(
            by = ReportCalculator.By.FULL,
            selectedAccounts = listOf("Account2")
        ) as ReportView.Full
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
    fun testCalculateFull_ifMonth1And4NotSelected() = runTest(dispatcher) {
        // Arrange
        setupSimpleCalculator()

        // Act
        val result = calculator.calculate(
            by = ReportCalculator.By.FULL,
            selectedMonths = listOf(2, 3)
        ) as ReportView.Full
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
    fun testCalculateFull_ifMonth4NotSelected() = runTest(dispatcher) {
        // Arrange
        setupSimpleCalculator()

        // Act
        val result = calculator.calculate(
            by = ReportCalculator.By.FULL,
            selectedMonths = listOf(1, 2, 3)
        ) as ReportView.Full
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
    fun testCalculateFull_ifMonth2AndAccount2NotSelected() = runTest(dispatcher) {
        // Arrange
        setupSimpleCalculator()

        // Act
        val result = calculator.calculate(
            by = ReportCalculator.By.FULL,
            selectedAccounts = listOf("Account1", "Account3"),
            selectedMonths = listOf(1, 3, 4)
        ) as ReportView.Full
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
    fun testCalculateByAccounts_ifMonth2AndAccount2NotSelected() = runTest(dispatcher) {
        // Arrange
        setupSimpleCalculator()

        // Act
        val result = (calculator.calculate(
            by = ReportCalculator.By.ACCOUNT,
            selectedAccounts = listOf("Account1", "Account3"),
            selectedMonths = listOf(1, 3, 4)
        ) as ReportView.ByAccount)

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
    fun testCalculateByMonth_ifMonth2AndAccount2NotSelected() = runTest(dispatcher) {
        // Arrange
        setupSimpleCalculator()

        // Act
        val result = (calculator.calculate(
            by = ReportCalculator.By.MONTH,
            selectedAccounts = listOf("Account1", "Account3"),
            selectedMonths = listOf(1, 3, 4)
        ) as ReportView.ByMonth)

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

    /**
     *     |     Name      | Month 1 | Month 2 | Month 3 | Month 4 |  Total  |
     *     |---------------|---------|---------|---------|---------|---------|
     *     |     Total     |   40    |   50    |   65    |   84    |   239   |
     *     |   Account 1   |    6    |   11    |   16    |   45    |    78   |
     *     |  - Account 11 |    4    |    5    |    8    |   33    |    50   |
     *     |  - Account 12 |    2    |    6    |    8    |   12    |    28   |
     *     |  Account 2    |    7    |    8    |    9    |   10    |    34   |
     *     |  - Account 21 |    7    |    8    |    9    |   10    |    34   |
     *     |  Account 3    |   27    |   31    |   40    |   29    |   127   |
     *     |  - Account 31 |   12    |    8    |   12    |   10    |    42   |
     *     |  - Account 32 |   15    |   23    |   28    |   19    |    85   |
     */
    @Test
    fun testCalculateFullComplex_ifNoChangesMadeToAccountAndMonthSelections() =
        runTest(dispatcher) {
            // Arrange
            setupComplexCalculator()

            // Act
            val result = calculator.calculate(
                by = ReportCalculator.By.FULL
            ) as ReportView.Full
            val accountTotals = result.accountTotals

            // Assert Account 1
            val account1 = accountTotals[0]
            assert(78, account1.total)

            // Assert Account 2
            val account2 = accountTotals[1]
            assert(34, account2.total)

            // Assert Account 3
            val account3 = accountTotals[2]
            assert(127, account3.total)

            // Assert Totals row
            val totalsAccount = result.total
            assertEquals(4, totalsAccount.monthAmounts.size)
            assert(40, totalsAccount.monthAmounts[1])
            assert(50, totalsAccount.monthAmounts[2])
            assert(65, totalsAccount.monthAmounts[3])
            assert(84, totalsAccount.monthAmounts[4])
            assert(239, totalsAccount.total)
        }

    /**
     *     |     Name      | Month 1 | Month 2 | Month 3 | Month 4 |  Total  |
     *     |---------------|---------|---------|---------|---------|---------|
     *     |     Total     |   40    |   50    |   65    |   84    |   239   |
     *     |   Account 1   |    6    |   11    |   16    |   45    |    78   |
     *     |  - Account 11 |    4    |    5    |    8    |   33    |    50   |
     *     |  - Account 12 |    2    |    6    |    8    |   12    |    28   |
     *     |  Account 2    |    7    |    8    |    9    |   10    |    34   |
     *     |  - Account 21 |    7    |    8    |    9    |   10    |    34   |
     *     |  Account 3    |   27    |   31    |   40    |   29    |   127   |
     *     |  - Account 31 |   12    |    8    |   12    |   10    |    42   |
     *     |  - Account 32 |   15    |   23    |   28    |   19    |    85   |
     */
    @Test
    fun testCalculateByAccountComplex_ifNoChangesMadeToAccountAndMonthSelections() =
        runTest(dispatcher) {
            // Arrange
            setupComplexCalculator()

            // Act
            val result = (calculator.calculate(
                by = ReportCalculator.By.ACCOUNT
            ) as ReportView.ByAccount)

            val accountTotals = result.accountTotals

            // Assert Account 1
            val account1Total = accountTotals["Account1"]
            assert(78, account1Total)

            // Assert Account 2
            val account2Total = accountTotals["Account2"]
            assert(34, account2Total)

            // Assert Account 3
            val account3Total = accountTotals["Account3"]
            assert(127, account3Total)

            // Assert Totals row
            val totalsAccountTotal = result.total
            assert(239, totalsAccountTotal)
        }

    /**
     *     |     Name      | Month 1 | Month 2 | Month 3 | Month 4 |  Total  |
     *     |---------------|---------|---------|---------|---------|---------|
     *     |     Total     |   40    |   50    |   65    |   84    |   239   |
     *     |   Account 1   |    6    |   11    |   16    |   45    |    78   |
     *     |  - Account 11 |    4    |    5    |    8    |   33    |    50   |
     *     |  - Account 12 |    2    |    6    |    8    |   12    |    28   |
     *     |  Account 2    |    7    |    8    |    9    |   10    |    34   |
     *     |  - Account 21 |    7    |    8    |    9    |   10    |    34   |
     *     |  Account 3    |   27    |   31    |   40    |   29    |   127   |
     *     |  - Account 31 |   12    |    8    |   12    |   10    |    42   |
     *     |  - Account 32 |   15    |   23    |   28    |   19    |    85   |
     */
    @Test
    fun testCalculateByMonthComplex_ifNoChangesMadeToAccountAndMonthSelections() =
        runTest(dispatcher) {
            // Arrange
            setupComplexCalculator()

            // Act
            val result = (calculator.calculate(
                by = ReportCalculator.By.MONTH
            ) as ReportView.ByMonth)

            val monthTotals = result.monthTotals

            // Assert Month 1
            val month1Total = monthTotals[1]
            assert(40, month1Total)

            // Assert Month 2
            val month2Total = monthTotals[2]
            assert(50, month2Total)

            // Assert Month 3
            val month3Total = monthTotals[3]
            assert(65, month3Total)

            // Assert Month 4
            val month4Total = monthTotals[4]
            assert(84, month4Total)

            // Assert Totals row
            val totalsAccountTotal = result.total
            assert(239, totalsAccountTotal)
        }

    /**
     *     |     Name      | Month 1 | Month 2 | Month 3 | Month 4 |  Total  |
     *     |---------------|---------|---------|---------|---------|---------|
     *     |     Total     |   34    |   39    |   49    |   39    |   161   |
     *     |  Account 2    |    7    |    8    |    9    |   10    |    34   |
     *     |  - Account 21 |    7    |    8    |    9    |   10    |    34   |
     *     |  Account 3    |   27    |   31    |   40    |   29    |   127   |
     *     |  - Account 31 |   12    |    8    |   12    |   10    |    42   |
     *     |  - Account 32 |   15    |   23    |   28    |   19    |    85   |
     */
    @Test
    fun testCalculateFullComplex_ifAccount1NotSelected() = runTest(dispatcher) {
        // Arrange
        setupComplexCalculator()

        // Act
        val result = calculator.calculate(
            by = ReportCalculator.By.FULL,
            selectedAccounts = listOf("Account2", "Account3")
        ) as ReportView.Full
        val accountTotals = result.accountTotals

        // Assert Account 2
        val account2 = accountTotals[0]
        assert(34, account2.total)

        // Assert Account 3
        val account3 = accountTotals[1]
        assert(127, account3.total)

        // Assert Totals row
        val totalsAccount = result.total
        assert(34, totalsAccount.monthAmounts[1])
        assert(39, totalsAccount.monthAmounts[2])
        assert(49, totalsAccount.monthAmounts[3])
        assert(39, totalsAccount.monthAmounts[4])
        assert(161, totalsAccount.total)
    }

    /**
     *     |     Name      | Month 1 | Month 2 | Month 3 | Month 4 |  Total  |
     *     |---------------|---------|---------|---------|---------|---------|
     *     |     Total     |    7    |    8    |    9    |   10    |    34   |
     *     |  Account 2    |    7    |    8    |    9    |   10    |    34   |
     *     |  - Account 21 |    7    |    8    |    9    |   10    |    34   |
     */
    @Test
    fun testCalculateFullComplex_ifAccount1And3NotSelected() = runTest(dispatcher) {
        // Arrange
        setupComplexCalculator()

        // Act
        val result = calculator.calculate(
            by = ReportCalculator.By.FULL,
            selectedAccounts = listOf("Account2")
        ) as ReportView.Full
        val accountTotals = result.accountTotals

        // Assert Account 2
        val account2 = accountTotals[0]
        assert(34, account2.total)

        // Assert Totals row
        val totalsAccount = result.total
        assert(7, totalsAccount.monthAmounts[1])
        assert(8, totalsAccount.monthAmounts[2])
        assert(9, totalsAccount.monthAmounts[3])
        assert(10, totalsAccount.monthAmounts[4])
        assert(34, totalsAccount.total)
    }

    /**
     *     |     Name      | Month 2 | Month 3 |  Total  |
     *     |---------------|---------|---------|---------|
     *     |     Total     |   50    |   65    |   115   |
     *     |   Account 1   |   11    |   16    |    27   |
     *     |  - Account 11 |    5    |    8    |    13   |
     *     |  - Account 12 |    6    |    8    |    14   |
     *     |  Account 2    |    8    |    9    |    17   |
     *     |  - Account 21 |    8    |    9    |    17   |
     *     |  Account 3    |   31    |   40    |    71   |
     *     |  - Account 31 |    8    |   12    |    20   |
     *     |  - Account 32 |   23    |   28    |    51   |
     */
    @Test
    fun testCalculateFullComplex_ifMonth1And4NotSelected() = runTest(dispatcher) {
        // Arrange
        setupComplexCalculator()

        // Act
        val result = calculator.calculate(
            by = ReportCalculator.By.FULL,
            selectedMonths = listOf(2, 3)
        ) as ReportView.Full
        val accountTotals = result.accountTotals

        // Assert Account 1
        val account1 = accountTotals[0]
        assert(27, account1.total)

        // Assert Account 2
        val account2 = accountTotals[1]
        assert(17, account2.total)

        // Assert Account 3
        val account3 = accountTotals[2]
        assert(71, account3.total)

        // Assert Totals row
        val totalsAccount = result.total
        assertEquals(2, totalsAccount.monthAmounts.size)
        assert(50, totalsAccount.monthAmounts[2])
        assert(65, totalsAccount.monthAmounts[3])
        assert(115, totalsAccount.total)
    }

    /**
     *     |     Name      | Month 1 | Month 2 | Month 3 |  Total  |
     *     |---------------|---------|---------|---------|---------|
     *     |     Total     |   40    |   50    |   65    |   155   |
     *     |   Account 1   |    6    |   11    |   16    |    33   |
     *     |  - Account 11 |    4    |    5    |    8    |    17   |
     *     |  - Account 12 |    2    |    6    |    8    |    16   |
     *     |  Account 2    |    7    |    8    |    9    |    24   |
     *     |  - Account 21 |    7    |    8    |    9    |    24   |
     *     |  Account 3    |   27    |   31    |   40    |    98   |
     *     |  - Account 31 |   12    |    8    |   12    |    32   |
     *     |  - Account 32 |   15    |   23    |   28    |    66   |
     */
    @Test
    fun testCalculateFullComplex_ifMonth4NotSelected() = runTest(dispatcher) {
        // Arrange
        setupComplexCalculator()

        // Act
        val result = calculator.calculate(
            by = ReportCalculator.By.FULL,
            selectedMonths = listOf(1, 2, 3)
        ) as ReportView.Full
        val accountTotals = result.accountTotals

        // Assert Account 1
        val account1 = accountTotals[0]
        assert(33, account1.total)

        // Assert Account 2
        val account2 = accountTotals[1]
        assert(24, account2.total)

        // Assert Account 3
        val account3 = accountTotals[2]
        assert(98, account3.total)

        // Assert Totals row
        val totalsAccount = result.total
        assertEquals(3, totalsAccount.monthAmounts.size)
        assert(40, totalsAccount.monthAmounts[1])
        assert(50, totalsAccount.monthAmounts[2])
        assert(65, totalsAccount.monthAmounts[3])
        assert(155, totalsAccount.total)
    }

    /**
     *     |     Name      | Month 1 | Month 3 | Month 4 |  Total  |
     *     |---------------|---------|---------|---------|---------|
     *     |     Total     |   33    |   56    |   74    |   163   |
     *     |   Account 1   |    6    |   16    |   45    |    67   |
     *     |  - Account 11 |    4    |    8    |   33    |    45   |
     *     |  - Account 12 |    2    |    8    |   12    |    22   |
     *     |  Account 3    |   27    |   40    |   29    |    96   |
     *     |  - Account 31 |   12    |   12    |   10    |    34   |
     *     |  - Account 32 |   15    |   28    |   19    |    62   |
     */
    @Test
    fun testCalculateFullComplex_ifMonth2AndAccount2NotSelected() = runTest(dispatcher) {
        // Arrange
        setupComplexCalculator()

        // Act
        val result = calculator.calculate(
            by = ReportCalculator.By.FULL,
            selectedAccounts = listOf("Account1", "Account3"),
            selectedMonths = listOf(1, 3, 4)
        ) as ReportView.Full
        val accountTotals = result.accountTotals

        // Assert Account 1
        val account1 = accountTotals[0]
        assert(67, account1.total)

        // Assert Account 3
        val account3 = accountTotals[1]
        assert(96, account3.total)

        // Assert Totals row
        val totalsAccount = result.total
        assertEquals(3, totalsAccount.monthAmounts.size)
        assert(33, totalsAccount.monthAmounts[1])
        assert(56, totalsAccount.monthAmounts[3])
        assert(74, totalsAccount.monthAmounts[4])
        assert(163, totalsAccount.total)
    }

    /**
     *     |     Name      | Month 1 | Month 3 | Month 4 |  Total  |
     *     |---------------|---------|---------|---------|---------|
     *     |     Total     |   33    |   56    |   74    |   163   |
     *     |   Account 1   |    6    |   16    |   45    |    67   |
     *     |  - Account 11 |    4    |    8    |   33    |    45   |
     *     |  - Account 12 |    2    |    8    |   12    |    22   |
     *     |  Account 3    |   27    |   40    |   29    |    96   |
     *     |  - Account 31 |   12    |   12    |   10    |    34   |
     *     |  - Account 32 |   15    |   28    |   19    |    62   |
     *
     */
    @Test
    fun testCalculateByAccountsComplex_ifMonth2AndAccount2NotSelected() = runTest(dispatcher) {
        // Arrange
        setupComplexCalculator()

        // Act
        val result = (calculator.calculate(
            by = ReportCalculator.By.ACCOUNT,
            selectedAccounts = listOf("Account1", "Account3"),
            selectedMonths = listOf(1, 3, 4)
        ) as ReportView.ByAccount)

        val accountTotals = result.accountTotals

        // Assert Account 1
        val account1Amount = accountTotals["Account1"]
        assert(67, account1Amount)

        // Assert Account 3
        val account3Amount = accountTotals["Account3"]
        assert(96, account3Amount)

        // Assert Totals row
        val totalsAccountAmount = result.total
        assert(163, totalsAccountAmount)
    }

    /**
     *     |     Name      | Month 1 | Month 3 | Month 4 |  Total  |
     *     |---------------|---------|---------|---------|---------|
     *     |     Total     |   33    |   56    |   74    |    163   |
     *     |   Account 1   |    6    |   16    |   45    |    67   |
     *     |  - Account 11 |    4    |    8    |   33    |    45   |
     *     |  - Account 12 |    2    |    8    |   12    |    22   |
     *     |  Account 3    |   27    |   40    |   29    |    96   |
     *     |  - Account 31 |   12    |   12    |   10    |    34   |
     *     |  - Account 32 |   15    |   28    |   19    |    62   |
     *
     */
    @Test
    fun testCalculateByMonthComplex_ifMonth2AndAccount2NotSelected() = runTest(dispatcher) {
        // Arrange
        setupComplexCalculator()

        // Act
        val result = (calculator.calculate(
            by = ReportCalculator.By.MONTH,
            selectedAccounts = listOf("Account1", "Account3"),
            selectedMonths = listOf(1, 3, 4)
        ) as ReportView.ByMonth)

        val monthTotals = result.monthTotals

        // Assert Month 1
        val month1Total = monthTotals[1]
        assert(33, month1Total)

        // Assert Month 3
        val month3Total = monthTotals[3]
        assert(56, month3Total)

        // Assert Month 3
        val month4Total = monthTotals[4]
        assert(74, month4Total)

        // Assert Totals row
        val totalsAccountTotal = result.total
        assert(163, totalsAccountTotal)
    }

    private fun assert(expectedAmount: Int, actualAmount: BigDecimal?) {
        assertEquals(BigDecimal.fromInt(expectedAmount), actualAmount!!)
    }

    private fun setupSimpleCalculator() {
        calculator = ReportCalculator(getSimpleReport(), dispatcher)
    }

    private fun setupComplexCalculator() {
        calculator = ReportCalculator(getComplexReport(), dispatcher)
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
    private fun getSimpleReport(): Report {
        return Report(
            name = "Report",
            generatedAt = Clock.System.now(),
            fetchedAt = Clock.System.now(),
            accountTotal = AccountTotalWithTotal(
                name = "Expenses",
                fullName = "Expenses",
                children = listOf(
                    AccountTotalWithTotal(
                        name = "Account1",
                        fullName = "Account1",
                        children = listOf(),
                        monthAmounts = mapOf(
                            1 to BigDecimal.parseString("1"),
                            2 to BigDecimal.parseString("2"),
                            3 to BigDecimal.parseString("3"),
                            4 to BigDecimal.parseString("4")
                        )
                    ),
                    AccountTotalWithTotal(
                        name = "Account2",
                        fullName = "Account2",
                        children = listOf(),
                        monthAmounts = mapOf(
                            1 to BigDecimal.parseString("7"),
                            2 to BigDecimal.parseString("8"),
                            3 to BigDecimal.parseString("9"),
                            4 to BigDecimal.parseString("10")

                        )
                    ),
                    AccountTotalWithTotal(
                        name = "Account3",
                        fullName = "Account3",
                        children = listOf(),
                        monthAmounts = mapOf(
                            1 to BigDecimal.parseString("14"),
                            2 to BigDecimal.parseString("15"),
                            3 to BigDecimal.parseString("16"),
                            4 to BigDecimal.parseString("17")
                        )
                    )
                ),
                monthAmounts = mapOf<Int, BigDecimal>(),
                total = BigDecimal.ZERO
            ),
            type = ReportType.EXPENSE,
            year = 2023
        )
    }

    /**
     * Returns a report that looks like
     *
     *     |     Name      | Month 1 | Month 2 | Month 3 | Month 4 |
     *     |---------------|---------|---------|---------|---------|
     *     |   Account 1   |    6    |   11    |   16    |   45    |
     *     |  - Account 11 |    4    |    5    |    8    |   33    |
     *     |  - Account 12 |    2    |    6    |    8    |   12    |
     *     |  Account 2    |    7    |    8    |    9    |   10    |
     *     |  - Account 21 |    7    |    8    |    9    |   10    |
     *     |  Account 3    |   27    |   31    |   40    |   29    |
     *     |  - Account 31 |   12    |    8    |   12    |   10    |
     *     |  - Account 32 |   15    |   23    |   28    |   19    |
     */
    private fun getComplexReport(): Report {
        return Report(
            name = "Report",
            generatedAt = Clock.System.now(),
            fetchedAt = Clock.System.now(),
            accountTotal = AccountTotalWithTotal(
                name = "Expenses",
                fullName = "Expenses",
                children = listOf(
                    AccountTotalWithTotal(
                        name = "Account1",
                        fullName = "Account1",
                        children = listOf(
                            AccountTotalWithTotal(
                                name = "Account11",
                                fullName = "Account11",
                                children = listOf(),
                                monthAmounts = mapOf(
                                    1 to BigDecimal.parseString("4"),
                                    2 to BigDecimal.parseString("5"),
                                    3 to BigDecimal.parseString("8"),
                                    4 to BigDecimal.parseString("33")
                                )
                            ), AccountTotalWithTotal(
                                name = "Account12",
                                fullName = "Account12",
                                children = listOf(),
                                monthAmounts = mapOf(
                                    1 to BigDecimal.parseString("2"),
                                    2 to BigDecimal.parseString("6"),
                                    3 to BigDecimal.parseString("8"),
                                    4 to BigDecimal.parseString("12")
                                )
                            )
                        ),
                        monthAmounts = mapOf(
                            1 to BigDecimal.parseString("6"),
                            2 to BigDecimal.parseString("11"),
                            3 to BigDecimal.parseString("16"),
                            4 to BigDecimal.parseString("45")
                        )
                    ),
                    AccountTotalWithTotal(
                        name = "Account2",
                        fullName = "Account2",
                        children = listOf(
                            AccountTotalWithTotal(
                                name = "Account21",
                                fullName = "Account21",
                                children = listOf(),
                                monthAmounts = mapOf(
                                    1 to BigDecimal.parseString("7"),
                                    2 to BigDecimal.parseString("8"),
                                    3 to BigDecimal.parseString("9"),
                                    4 to BigDecimal.parseString("10")

                                )
                            )
                        ),
                        monthAmounts = mapOf(
                            1 to BigDecimal.parseString("7"),
                            2 to BigDecimal.parseString("8"),
                            3 to BigDecimal.parseString("9"),
                            4 to BigDecimal.parseString("10")

                        )
                    ),
                    AccountTotalWithTotal(
                        name = "Account3",
                        fullName = "Account3",
                        children = listOf(
                            AccountTotalWithTotal(
                                name = "Account31",
                                fullName = "Account31",
                                children = listOf(),
                                monthAmounts = mapOf(
                                    1 to BigDecimal.parseString("12"),
                                    2 to BigDecimal.parseString("8"),
                                    3 to BigDecimal.parseString("12"),
                                    4 to BigDecimal.parseString("10")
                                )
                            ), AccountTotalWithTotal(
                                name = "Account32",
                                fullName = "Account32",
                                children = listOf(),
                                monthAmounts = mapOf(
                                    1 to BigDecimal.parseString("15"),
                                    2 to BigDecimal.parseString("23"),
                                    3 to BigDecimal.parseString("28"),
                                    4 to BigDecimal.parseString("19")
                                )
                            )
                        ),
                        monthAmounts = mapOf(
                            1 to BigDecimal.parseString("27"),
                            2 to BigDecimal.parseString("31"),
                            3 to BigDecimal.parseString("40"),
                            4 to BigDecimal.parseString("29")
                        )
                    )
                ),
                monthAmounts = mapOf<Int, BigDecimal>(),
                total = BigDecimal.ZERO
            ),
            type = ReportType.EXPENSE,
            year = 2023
        )
    }
}