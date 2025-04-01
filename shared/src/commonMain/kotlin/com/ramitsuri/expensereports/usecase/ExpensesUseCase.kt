package com.ramitsuri.expensereports.usecase

import com.ramitsuri.expensereports.log.logI
import com.ramitsuri.expensereports.model.MonthYear
import com.ramitsuri.expensereports.model.Period
import com.ramitsuri.expensereports.model.ReportNames
import com.ramitsuri.expensereports.model.sumPeriod
import com.ramitsuri.expensereports.repository.MainRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import java.math.BigDecimal

class ExpensesUseCase(
    private val mainRepository: MainRepository,
    private val clock: Clock = Clock.System,
    private val timeZone: TimeZone = TimeZone.currentSystemDefault(),
) {
    operator fun invoke(forPeriods: List<Period>): Flow<Map<Period, BigDecimal>> {
        return mainRepository.getReport(
            reportName = ReportNames.AfterDeductionsExpenses.name,
            monthYears = Period.AllTime.toMonthYears(MonthYear.now(clock, timeZone)),
        ).map { report ->
            if (report == null) {
                logI(TAG) { "AfterDeductionsExpenses report is null" }
                return@map emptyMap()
            }
            val expenses = report.accounts.firstOrNull()?.monthTotals
            if (expenses == null) {
                logI(TAG) { "expenses account is null" }
                return@map emptyMap()
            }
            forPeriods.map { period ->
                val now = MonthYear.now(clock, timeZone)
                val totals = expenses.sumPeriod(period, now)
                period to totals
            }.associate { it }
        }
    }

    companion object {
        private const val TAG = "ExpensesUseCase"
    }
}
