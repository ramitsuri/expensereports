package com.ramitsuri.expensereports.utils

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus

fun LocalDate.startOfMonth(): LocalDate {
    return LocalDate(
        year = this.year,
        month = this.month,
        dayOfMonth = 1
    )
}

fun LocalDate.endOfMonth(): LocalDate {
    return startOfMonth()
        .plus(DatePeriod(months = 1))
        .minus(DatePeriod(days = 1))
}