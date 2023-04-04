package com.ramitsuri.expensereports.android.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.ramitsuri.expensereports.android.R
import com.ramitsuri.expensereports.utils.format
import com.ramitsuri.expensereports.utils.timeDateMonthYear
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import java.time.Duration
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

// Jan ’23
fun LocalDate.monthYear(): String {
    return DateTimeFormatter.ofPattern("MMM ’uu").format(this.toJavaLocalDate())
}

/**
 * As of 4 PM yesterday
 * As of 4:30 PM today
 * As of 4:30 PM Aug 21
 * As of 4:30 PM Aug 21, 2022
 */
@Composable
fun Instant.timeAndDay(
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
    now: LocalDateTime = Clock.System.now().toLocalDateTime(timeZone)
): String {
    return this.toLocalDateTime(timeZone).timeAndDay(timeZone = timeZone, now = now)
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
            stringResource(id = R.string.reports_generated_at_today_format, time)
        }
        1L -> {
            val time = this.timeOnly(timeZone, now)
            stringResource(id = R.string.reports_generated_at_yesterday_format, time)
        }
        else -> {
            val time = this.timeDateMonthYear(timeZone, now)
            stringResource(id = R.string.reports_generated_at_date_format, time)
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
 * 08/10/2021
 */
fun LocalDate.monthDateYear(
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
    now: LocalDate = Clock.System.now().toLocalDateTime(timeZone).date
): String {
    val (formatSameYear, formatDifferentYear) = Pair("MM/dd", "MM/dd/uuuu")
    val pattern = if (year == now.year) {
        formatSameYear
    } else {
        formatDifferentYear
    }
    val jvmToFormat = toJavaLocalDate()
    val formatter = DateTimeFormatter
        .ofPattern(pattern)
        .withLocale(Locale.getDefault())
    return formatter.format(jvmToFormat)
}