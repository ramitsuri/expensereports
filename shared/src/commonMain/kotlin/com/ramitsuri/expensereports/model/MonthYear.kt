package com.ramitsuri.expensereports.model

import com.ramitsuri.expensereports.network.MonthYearSerializer
import com.ramitsuri.expensereports.utils.minus
import com.ramitsuri.expensereports.utils.nowLocal
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimePeriod
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.math.BigDecimal

@Serializable(with = MonthYearSerializer::class)
data class MonthYear(
    @SerialName("month")
    val month: Month,
    @SerialName("year")
    val year: Int,
) : Comparable<MonthYear> {
    fun next(): MonthYear {
        return if (month == Month.DECEMBER) {
            MonthYear(year = year + 1, month = Month.JANUARY)
        } else {
            MonthYear(year = year, month = Month.of(month.number + 1))
        }
    }

    fun previous(): MonthYear {
        return if (month == Month.JANUARY) {
            MonthYear(year = year - 1, month = Month.DECEMBER)
        } else {
            MonthYear(year = year, month = Month.of(month.number - 1))
        }
    }

    constructor(localDateTime: LocalDateTime) : this(
        month = localDateTime.month,
        year = localDateTime.year,
    )

    fun string() = "$year-${month.number.toString().padStart(2, '0')}"

    override operator fun compareTo(other: MonthYear): Int {
        return if (year < other.year) {
            -1
        } else if (year > other.year) {
            1
        } else {
            if (month < other.month) {
                -1
            } else if (month > other.month) {
                1
            } else {
                0
            }
        }
    }

    companion object {
        fun now(
            clock: Clock = Clock.System,
            timeZone: TimeZone = TimeZone.currentSystemDefault(),
        ): MonthYear {
            return clock.nowLocal(timeZone).let { MonthYear(year = it.year, month = it.month) }
        }

        fun fromString(string: String) =
            string.split("-").let {
                MonthYear(year = it[0].toInt(), month = Month(it[1].toInt()))
            }
    }
}

fun ClosedRange<MonthYear>.toList(): List<MonthYear> {
    val list = mutableListOf<MonthYear>()
    var current = start
    while (current <= endInclusive) {
        list.add(current)
        current = current.next()
    }
    return list.toList()
}

fun Map<MonthYear, BigDecimal>.sum(): BigDecimal {
    var sum = BigDecimal.ZERO
    forEach { (_, value) ->
        sum += value
    }
    return sum
}

fun Map<MonthYear, BigDecimal>.sumPeriod(
    period: Period,
    now: MonthYear,
): BigDecimal {
    return when (period) {
        is Period.AllTime -> {
            val allTime = MonthYear(Month.JANUARY, 2021)
            filterKeys { monthYear -> monthYear >= allTime }
        }

        is Period.LastThreeYears -> {
            val lastThreeYears = now.minus(DateTimePeriod(years = 3))
            filterKeys { monthYear -> monthYear >= lastThreeYears }
        }

        is Period.OneYear -> {
            val oneYear = now.minus(DateTimePeriod(years = 1))
            filterKeys { monthYear -> monthYear >= oneYear }
        }

        is Period.PreviousMonth -> {
            val previousMonthYear = now.previous()
            filterKeys { monthYear -> monthYear == previousMonthYear }
        }

        is Period.ThisMonth -> {
            filterKeys { monthYear -> monthYear == now }
        }

        is Period.ThisYear -> {
            filterKeys { (_, year) -> year == now.year }
        }
    }.sum()
}
