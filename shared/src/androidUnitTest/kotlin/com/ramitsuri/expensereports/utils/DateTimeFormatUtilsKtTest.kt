package com.ramitsuri.expensereports.utils

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import org.junit.Test
import kotlin.test.assertEquals

class DateTimeFormatUtilsKtTest {
    private val timeZone = TimeZone.of("America/New_York")
    private val now = LocalDateTime.parse("2023-07-25T12:30:00")

    @Test
    fun testTimeDateMonthYear() {
        assert("2023-07-25T12:30:00", "12:30 PM Jul 25")
        assert("2023-07-25T00:30:00", "12:30 AM Jul 25")
        assert("2023-07-25T23:59:00", "11:59 PM Jul 25")

        assert("2022-07-25T12:30:00", "12:30 PM Jul 25, 2022")
        assert("2022-07-25T00:30:00", "12:30 AM Jul 25, 2022")
        assert("2022-07-25T23:59:00", "11:59 PM Jul 25, 2022")

        assert("2023-07-25T12:00:00", "12 PM Jul 25")
        assert("2023-07-25T00:00:00", "12 AM Jul 25")
        assert("2023-07-25T23:00:00", "11 PM Jul 25")

        assert("2022-07-25T12:00:00", "12 PM Jul 25, 2022")
        assert("2022-07-25T00:00:00", "12 AM Jul 25, 2022")
        assert("2022-07-25T23:00:00", "11 PM Jul 25, 2022")

    }

    private fun assert(stringDateTime: String, expected: String) {
        val localDateTime = LocalDateTime.parse(stringDateTime)
        val formatted = localDateTime.timeDateMonthYear(timeZone, now)
        assertEquals(expected, formatted)
    }
}