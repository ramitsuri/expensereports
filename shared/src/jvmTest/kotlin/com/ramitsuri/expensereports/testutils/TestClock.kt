package com.ramitsuri.expensereports.testutils

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Instant

class TestClock : Clock {
    var timeZone: TimeZone = TimeZone.UTC
    var nowLocal: String = Instant.DISTANT_PAST.toLocalDateTime(timeZone).toString()

    override fun now(): Instant {
        return LocalDateTime.parse(nowLocal).toInstant(timeZone)
    }
}
