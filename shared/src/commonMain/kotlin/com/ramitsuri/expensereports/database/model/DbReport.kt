package com.ramitsuri.expensereports.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(
    tableName = "db_report",
    primaryKeys = ["name"]
)
internal data class DbReport(
    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "with_cumulative_balance")
    val withCumulativeBalance: Boolean,
)
