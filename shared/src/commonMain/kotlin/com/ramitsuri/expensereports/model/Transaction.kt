package com.ramitsuri.expensereports.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.math.BigDecimal

@Serializable
@Entity(tableName = "db_transaction")
data class Transaction(
    @SerialName("id")
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    @SerialName("splits")
    @ColumnInfo(name = "splits")
    val splits: List<TransactionSplit>,
    @SerialName("date")
    @ColumnInfo(name = "date")
    val date: LocalDate,
    @SerialName("description")
    @ColumnInfo(name = "description")
    val description: String,
) {
    val total: BigDecimal
        get() = splits.filter { it.amount < BigDecimal.ZERO }.sumOf { it.amount.abs() }

    fun fromSplits(): List<TransactionSplit> {
        return splits.filter { it.amount < BigDecimal.ZERO }
    }

    fun toSplits(): List<TransactionSplit> {
        return splits.filter { it.amount >= BigDecimal.ZERO }
    }
}
