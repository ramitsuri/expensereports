package com.ramitsuri.expensereports.utils

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * 4 PM Aug 10
 * 4:30 PM Aug 10
 * 4:30 PM Aug 10, 2021
 */
fun LocalDateTime.timeDateMonthYear(): String {
    return timeDateMonthYear(
        timeZone = TimeZone.currentSystemDefault(),
        now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    )
}

/**
 * 4 PM Aug 10
 * 4:30 PM Aug 10
 * 4:30 PM Aug 10, 2021
 */
expect fun LocalDateTime.timeDateMonthYear(
    timeZone: TimeZone,
    now: LocalDateTime
): String