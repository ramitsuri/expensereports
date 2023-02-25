package com.ramitsuri.expensereports.android.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.ramitsuri.expensereports.android.R
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toJavaZoneId
import kotlinx.datetime.toLocalDateTime
import java.time.Duration
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

// Jan ’23
fun LocalDate.monthYear(): String {
    return DateTimeFormatter.ofPattern("MMM ’uu").format(this.toJavaLocalDate())
}

// This month
// Last month
// January 2023
@Composable
fun LocalDate.homeMonthYear(
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
    now: LocalDate = Clock.System.now().toLocalDateTime(timeZone).date
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

/**
 * As of 4 PM yesterday
 * As of 4:30 PM today
 * As of 4:30 PM Aug 21
 * As of 4:30 PM Aug 21, 2022
 */
@Composable
fun LocalDateTime.timeAndDay(
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
    now: LocalDateTime = Clock.System.now().toLocalDateTime(timeZone)
): String {
    val nowTruncated = now.toJavaLocalDateTime().truncatedTo(ChronoUnit.DAYS)
    val toFormatTruncated = this.toJavaLocalDateTime().truncatedTo(ChronoUnit.DAYS)

    return when (Duration.between(nowTruncated, toFormatTruncated).toDays()) {
        0L -> {
            val time = this.timeOnly(timeZone, now)
            stringResource(id = R.string.accounts_as_of_today_format, time)
        }
        1L -> {
            val time = this.timeOnly(timeZone, now)
            stringResource(id = R.string.accounts_as_of_yesterday_format, time)
        }
        else -> {
            val time = this.timeDateMonthYear(timeZone, now)
            stringResource(id = R.string.accounts_as_of_date_format, time)
        }
    }
}

/**
 * 4 PM
 * 4:30 PM
 */
fun LocalDateTime.timeOnly(
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
    now: LocalDateTime = Clock.System.now().toLocalDateTime(timeZone)
): String {
    val (formatSameYear, formatDifferentYear) = if (this.minute == 0) {
        Pair("K a", "K a")
    } else {
        Pair("K:mm a", "K:mm a")
    }
    return format(
        this,
        now,
        timeZone,
        formatSameYear,
        formatDifferentYear
    )
}

/**
 * 4 PM Aug 10
 * 4:30 PM Aug 10
 * 4:30 PM Aug 10, 2021
 */
fun LocalDateTime.timeDateMonthYear(
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
    now: LocalDateTime = Clock.System.now().toLocalDateTime(timeZone)
): String {
    val (formatSameYear, formatDifferentYear) = if (this.minute == 0) {
        Pair("K a MMM d", "K a MMM d, uuuu")
    } else {
        Pair("K:mm a MMM d", "K:mm a MMM d, uuuu")
    }
    return format(
        this,
        now,
        timeZone,
        formatSameYear,
        formatDifferentYear
    )
}

private fun format(
    toFormat: LocalDateTime,
    now: LocalDateTime,
    timeZone: TimeZone,
    formatSameYear: String,
    formatDifferentYear: String
): String {
    val jvmToFormat = toFormat.toJavaLocalDateTime()
    val pattern = if (toFormat.year == now.year) {
        formatSameYear
    } else {
        formatDifferentYear
    }
    val formatter = DateTimeFormatter
        .ofPattern(pattern)
        .withLocale(Locale.getDefault())
        .withZone(timeZone.toJavaZoneId())
    return formatter.format(jvmToFormat)
}