package com.ramitsuri.expensereports.utils

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toJavaZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

actual fun LocalDateTime.timeDateMonthYear(
    timeZone: TimeZone,
    now: LocalDateTime
): String {
    val (formatSameYear, formatDifferentYear) = if (this.minute == 0) {
        Pair("h a MMM d", "h a MMM d, uuuu")
    } else {
        Pair("h:mm a MMM d", "h:mm a MMM d, uuuu")
    }
    return format(
        this,
        now,
        timeZone,
        formatSameYear,
        formatDifferentYear
    )
}

fun format(
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