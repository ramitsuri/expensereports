package com.ramitsuri.expensereports.usecase

import com.ramitsuri.expensereports.log.logI
import com.ramitsuri.expensereports.model.MonthYear
import com.ramitsuri.expensereports.model.Period
import com.ramitsuri.expensereports.model.ReportNames
import com.ramitsuri.expensereports.model.sumPeriod
import com.ramitsuri.expensereports.repository.MainRepository
import com.ramitsuri.expensereports.utils.divideForCalculation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import java.math.BigDecimal

/**
 * Income here is only the salary and not income from other means like rewards, which is the case in
 * income use case.
 */
class SavingsRateUseCase(
    private val mainRepository: MainRepository,
    private val clock: Clock = Clock.System,
    private val timeZone: TimeZone = TimeZone.currentSystemDefault(),
) {
    operator fun invoke(forPeriods: List<Period>): Flow<Map<Period, SavingsRate>> {
        return mainRepository.getReport(
            reportName = ReportNames.SavingsRate.name,
            monthYears = Period.AllTime.toMonthYears(MonthYear.now(clock, timeZone)),
        ).map { report ->
            if (report == null) {
                logI(TAG) { "SavingsRate report is null" }
                return@map emptyMap()
            }
            val incomes = report.accounts.first { it.order == 0 }.monthTotals
            val taxes = report.accounts.first { it.order == 1 }.monthTotals
            val expenses = report.accounts.first { it.order == 2 }.monthTotals
            val now = MonthYear.now(clock, timeZone)
            forPeriods.map { period ->
                val income = incomes.sumPeriod(period, now)
                val expense = expenses.sumPeriod(period, now)
                val tax = taxes.sumPeriod(period, now)
                val savingsRate = SavingsRate(income, expense, tax)
                period to savingsRate
            }.associate { it }
        }
    }

    data class SavingsRate(
        val income: BigDecimal,
        val expenses: BigDecimal,
        val taxes: BigDecimal,
    ) {
        val savingsRate: BigDecimal
            get() {
                val afterTaxIncome = income - taxes
                val savings = afterTaxIncome - expenses
                if (afterTaxIncome.compareTo(BigDecimal.ZERO) == 0) {
                    return BigDecimal.ZERO
                }
                return savings
                    .divideForCalculation(afterTaxIncome)
            }
    }

    companion object {
        private const val TAG = "SavingsRateUseCase"
    }
}
