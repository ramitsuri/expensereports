package com.ramitsuri.expensereports.model

import androidx.compose.runtime.Composable
import com.ramitsuri.expensereports.utils.minus
import expensereports.shared.generated.resources.Res
import expensereports.shared.generated.resources.period_all
import expensereports.shared.generated.resources.period_last_three_years
import expensereports.shared.generated.resources.period_one_year
import expensereports.shared.generated.resources.period_previous_month
import expensereports.shared.generated.resources.period_this_month
import expensereports.shared.generated.resources.period_this_year
import kotlinx.datetime.DateTimePeriod
import kotlinx.datetime.Month
import org.jetbrains.compose.resources.stringResource

sealed interface Period {
    data object ThisMonth : Period

    data object PreviousMonth : Period

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

                is PreviousMonth -> {
                    return listOf(now.previous())
                }

                is ThisMonth -> {
                    return listOf(now)
                }
            }
        return (start..now).toList()
    }

    companion object {
        val all =
            listOf(
                ThisMonth,
                PreviousMonth,
                ThisYear,
                OneYear,
                LastThreeYears,
                AllTime,
            )
    }
}

@Composable
fun Period.formatted() =
    when (this) {
        is Period.ThisYear -> stringResource(Res.string.period_this_year)
        is Period.OneYear -> stringResource(Res.string.period_one_year)
        is Period.LastThreeYears -> stringResource(Res.string.period_last_three_years)
        is Period.AllTime -> stringResource(Res.string.period_all)
        is Period.ThisMonth -> stringResource(Res.string.period_this_month)
        is Period.PreviousMonth -> stringResource(Res.string.period_previous_month)
    }
