package com.ramitsuri.expensereports.model

import com.ramitsuri.expensereports.utils.minus
import kotlinx.datetime.DateTimePeriod
import kotlinx.datetime.Month

sealed interface Period {
    data object ThisYear : Period

    data object OneYear : Period

    data object LastThreeYears : Period

    data object AllTime : Period

    fun toMonthYears(now: MonthYear): List<MonthYear> {
        val start =
            when (this) {
                is AllTime -> {
                    MonthYear(Month.JANUARY, 2021)
                }

                is LastThreeYears -> {
                    now.minus(DateTimePeriod(years = 3))
                }

                is OneYear -> {
                    now.minus(DateTimePeriod(years = 1))
                }

                is ThisYear -> {
                    now.copy(month = Month.JANUARY)
                }
            }
        return (start..now).toList()
    }
}
