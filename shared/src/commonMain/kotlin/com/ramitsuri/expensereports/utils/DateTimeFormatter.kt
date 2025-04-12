package com.ramitsuri.expensereports.utils

import androidx.compose.runtime.Composable
import com.ramitsuri.expensereports.model.MonthYear
import expensereports.shared.generated.resources.Res
import expensereports.shared.generated.resources.days_ago_format
import expensereports.shared.generated.resources.hours_ago_format
import expensereports.shared.generated.resources.just_now
import expensereports.shared.generated.resources.minutes_ago_format
import expensereports.shared.generated.resources.month_names_long
import expensereports.shared.generated.resources.month_names_short
import expensereports.shared.generated.resources.one_day_ago
import expensereports.shared.generated.resources.one_hour_ago
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.getStringArray
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun friendlyDate(
    publishedDateTime: Instant,
    now: Instant = Clock.System.now(),
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
): String {
    val durationSincePublished = now - publishedDateTime
    val minutes = durationSincePublished.inWholeMinutes
    val hours = durationSincePublished.inWholeHours
    val days = durationSincePublished.inWholeDays
    when {
        minutes <= 1L -> {
            return stringResource(Res.string.just_now)
        }

        minutes < 60 -> {
            return stringResource(Res.string.minutes_ago_format, minutes.toInt())
        }

        hours < 2 -> {
            return stringResource(Res.string.one_hour_ago)
        }

        hours < 24 -> {
            return stringResource(Res.string.hours_ago_format, hours)
        }

        days < 2 -> {
            return stringResource(Res.string.one_day_ago)
        }

        days < 7 -> {
            return stringResource(Res.string.days_ago_format, days)
        }
    }
    val monthNames = monthNames()
    val format =
        LocalDateTime.Format {
            monthName(monthNames)
            char(' ')
            dayOfMonth()
            if (now.toLocalDateTime(timeZone).year != publishedDateTime.toLocalDateTime(timeZone).year) {
                char(',')
                char(' ')
                year()
            }
        }
    return publishedDateTime
        .toLocalDateTime(timeZone)
        .format(format)
}

suspend fun MonthYear.formatted(): String {
    val monthNames = monthNamesSuspend()
    val format =
        LocalDate.Format {
            monthName(monthNames)
            char(' ')
            yearTwoDigits(2000)
        }
    return toLocalDateTime()
        .date
        .format(format)
}

@Composable
fun fullLocalDateTime(
    dateTime: Instant,
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
): String {
    val monthNames = monthNames()
    val format =
        LocalDateTime.Format {
            monthName(monthNames)
            char(' ')
            dayOfMonth()
            char(',')
            hour()
            char(':')
            minute()
            char(':')
            second()
        }
    return dateTime
        .toLocalDateTime(timeZone)
        .format(format)
}

@Composable
private fun monthNames(useShortNames: Boolean = true): MonthNames {
    return MonthNames(
        if (useShortNames) {
            stringArrayResource(Res.array.month_names_short)
        } else {
            stringArrayResource(Res.array.month_names_long)
        },
    )
}

private suspend fun monthNamesSuspend(useShortNames: Boolean = true): MonthNames {
    return MonthNames(
        if (useShortNames) {
            getStringArray(Res.array.month_names_short)
        } else {
            getStringArray(Res.array.month_names_long)
        },
    )
}
