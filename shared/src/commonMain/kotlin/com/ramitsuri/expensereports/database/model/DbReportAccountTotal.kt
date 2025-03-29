package com.ramitsuri.expensereports.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import com.ramitsuri.expensereports.model.MonthYear
import java.math.BigDecimal

@Entity(
    tableName = "db_report_account_total",
    primaryKeys = ["report_name", "account_name", "month_year"]
)
internal data class DbReportAccountTotal(
    @ColumnInfo(name = "report_name")
    val reportName: String,

    @ColumnInfo(name = "account_name")
    val accountName: String,

    @ColumnInfo(name = "month_year")
    val monthYear: MonthYear,

    @ColumnInfo(name = "total")
    val total: BigDecimal,
)
