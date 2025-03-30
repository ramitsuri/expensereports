package com.ramitsuri.expensereports.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import com.ramitsuri.expensereports.network.BigDecimalSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.math.BigDecimal

@Serializable
@Entity(tableName = "db_current_balance", primaryKeys = ["name", "group_name"])
data class CurrentBalance(
    @SerialName("name")
    @ColumnInfo(name = "name")
    val name: String,
    @Serializable(with = BigDecimalSerializer::class)
    @SerialName("balance")
    @ColumnInfo(name = "balance")
    val balance: BigDecimal,
    @SerialName("group_name")
    @ColumnInfo(name = "group_name")
    val groupName: String,
)
