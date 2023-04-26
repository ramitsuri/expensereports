package com.ramitsuri.expensereports.utils

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toNSDateComponents
import kotlinx.datetime.toNSTimeZone
import platform.Foundation.NSCalendar
import platform.Foundation.NSDateFormatter

actual fun LocalDateTime.timeDateMonthYear(
    timeZone: TimeZone,
    now: LocalDateTime
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

actual fun LocalDate.monthDateYear(timeZone: TimeZone, now: LocalDate): String {
    val (formatSameYear, formatDifferentYear) = Pair("MM/dd", "MM/dd/uuuu")
    val pattern = if (year == now.year) {
        formatSameYear
    } else {
        formatDifferentYear
    }
    val dateFormatter = NSDateFormatter()
    dateFormatter.dateFormat = pattern
    dateFormatter.timeZone = timeZone.toNSTimeZone()
    val date = NSCalendar.currentCalendar.dateFromComponents(toNSDateComponents()) ?: return ""
    return dateFormatter.stringFromDate(date)
}

fun format(
    toFormat: LocalDateTime,
    now: LocalDateTime,
    timeZone: TimeZone,
    formatSameYear: String,
    formatDifferentYear: String
): String {
    val pattern = if (toFormat.year == now.year) {
        formatSameYear
    } else {
        formatDifferentYear
    }
    val dateFormatter = NSDateFormatter()
    dateFormatter.dateFormat = pattern
    dateFormatter.timeZone = timeZone.toNSTimeZone()
    val date =
        NSCalendar.currentCalendar.dateFromComponents(toFormat.toNSDateComponents()) ?: return ""
    return dateFormatter.stringFromDate(date)
}