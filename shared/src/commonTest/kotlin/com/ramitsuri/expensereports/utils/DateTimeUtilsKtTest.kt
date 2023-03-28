package com.ramitsuri.expensereports.utils

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class DateTimeUtilsTest {

    @Test
    fun testStartOfMonth() {
        var date = LocalDate(year = 2023, monthNumber = 1, dayOfMonth = 31)
        var startOfMonth = LocalDate(year = 2023, monthNumber = 1, dayOfMonth = 1)
        assertEquals(startOfMonth, date.startOfMonth())

        date = LocalDate(year = 2023, monthNumber = 1, dayOfMonth = 1)
        startOfMonth = LocalDate(year = 2023, monthNumber = 1, dayOfMonth = 1)
        assertEquals(startOfMonth, date.startOfMonth())

        date = LocalDate(year = 2023, monthNumber = 1, dayOfMonth = 10)
        startOfMonth = LocalDate(year = 2023, monthNumber = 1, dayOfMonth = 1)
        assertEquals(startOfMonth, date.startOfMonth())

        date = LocalDate(year = 2024, monthNumber = 2, dayOfMonth = 29)
        startOfMonth = LocalDate(year = 2024, monthNumber = 2, dayOfMonth = 1)
        assertEquals(startOfMonth, date.startOfMonth())
    }

    @Test
    fun testEndOfMonth() {
        var date = LocalDate(year = 2023, monthNumber = 1, dayOfMonth = 31)
        var endOfMonth = LocalDate(year = 2023, monthNumber = 1, dayOfMonth = 31)
        assertEquals(endOfMonth, date.endOfMonth())

        date = LocalDate(year = 2023, monthNumber = 1, dayOfMonth = 1)
        endOfMonth = LocalDate(year = 2023, monthNumber = 1, dayOfMonth = 31)
        assertEquals(endOfMonth, date.endOfMonth())

        date = LocalDate(year = 2023, monthNumber = 1, dayOfMonth = 10)
        endOfMonth = LocalDate(year = 2023, monthNumber = 1, dayOfMonth = 31)
        assertEquals(endOfMonth, date.endOfMonth())

        date = LocalDate(year = 2024, monthNumber = 2, dayOfMonth = 1)
        endOfMonth = LocalDate(year = 2024, monthNumber = 2, dayOfMonth = 29)
        assertEquals(endOfMonth, date.endOfMonth())

        date = LocalDate(year = 2024, monthNumber = 2, dayOfMonth = 10)
        endOfMonth = LocalDate(year = 2024, monthNumber = 2, dayOfMonth = 29)
        assertEquals(endOfMonth, date.endOfMonth())

        date = LocalDate(year = 2024, monthNumber = 2, dayOfMonth = 29)
        endOfMonth = LocalDate(year = 2024, monthNumber = 2, dayOfMonth = 29)
        assertEquals(endOfMonth, date.endOfMonth())
    }
}