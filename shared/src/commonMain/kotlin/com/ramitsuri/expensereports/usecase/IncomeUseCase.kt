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

/**
 * Income here is salary plus income from other means like rewards, which is not the case for income
 * in savings rate use case.
 */
class IncomeUseCase(
    private val mainRepository: MainRepository,
    private val clock: Clock,
    private val timeZone: TimeZone,
) {
    operator fun invoke(forPeriods: List<Period>): Flow<Map<Period, BigDecimal>> {
        return mainRepository.getReport(
            reportName = ReportNames.IncomeWithoutGains.name,
            monthYears = Period.AllTime.toMonthYears(MonthYear.now(clock, timeZone)),
        ).map { report ->
            if (report == null) {
                logI(TAG) { "IncomeWithoutGains report is null" }
                return@map emptyMap()
            }
            val incomes = report.accounts.firstOrNull()?.monthTotals
            if (incomes == null) {
                logI(TAG) { "income account is null" }
                return@map emptyMap()
            }
            forPeriods.map { period ->
                val now = MonthYear.now(clock, timeZone)
                val totals = incomes.sumPeriod(period, now)
                period to totals
            }.associate { it }
        }
    }

    companion object {
        private const val TAG = "IncomeUseCases"
    }
}
