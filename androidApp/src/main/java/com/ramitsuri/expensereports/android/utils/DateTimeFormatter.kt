package com.ramitsuri.expensereports.android.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.ramitsuri.expensereports.android.R
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toLocalDateTime
import java.time.format.DateTimeFormatter

fun LocalDate.monthYear(): String {
    return DateTimeFormatter.ofPattern("MMM ’uu").format(this.toJavaLocalDate())
}

@Composable
fun LocalDate.homeMonthYear(
    now: LocalDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
): String {
    return when (now.minus(other = this).months) {
        0 -> {
            stringResource(id = R.string.home_this_month)
        }
        1 -> {
            stringResource(id = R.string.home_last_month)
        }
        else -> {
            DateTimeFormatter.ofPattern("MMMM uuuu").format(this.toJavaLocalDate())
        }
    }
}