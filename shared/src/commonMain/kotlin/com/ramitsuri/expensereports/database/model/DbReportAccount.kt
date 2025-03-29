package com.ramitsuri.expensereports.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(
    tableName = "db_report_account",
    primaryKeys = ["report_name", "account_name"]
)
internal data class DbReportAccount(
    @ColumnInfo(name = "report_name")
    val reportName: String,

    @ColumnInfo(name = "account_name")
    val accountName: String,

    @ColumnInfo(name = "order")
    val order: Int,
)