package com.ramitsuri.expensereports.database

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import com.ramitsuri.expensereports.model.MonthYear
import com.ramitsuri.expensereports.model.TransactionSplit
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import java.math.BigDecimal

@ProvidedTypeConverter
class DatabaseConverters(
    private val json: Json,
) {
    @TypeConverter
    fun toBigDecimal(string: String): BigDecimal {
        return BigDecimal(string)
    }

    @TypeConverter
    fun fromBigDecimal(bigDecimal: BigDecimal): String {
        return bigDecimal.toString()
    }

    @TypeConverter
    fun toMonthYear(string: String): MonthYear {
        return MonthYear.fromString(string)
    }

    @TypeConverter
    fun fromMonthYear(monthYear: MonthYear): String {
        return monthYear.string()
    }

    @TypeConverter
    fun toLocalDate(string: String): LocalDate {
        return LocalDate.parse(string)
    }

    @TypeConverter
    fun fromLocalDate(localDate: LocalDate): String {
        return localDate.toString()
    }

    @TypeConverter
    fun toTxSplitList(string: String): List<TransactionSplit> {
        return json.decodeFromString(string)
    }

    @TypeConverter
    fun fromTxSplitList(list: List<TransactionSplit>): String {
        return json.encodeToString(list)
    }
}
