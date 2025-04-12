package com.ramitsuri.expensereports.model

import com.ramitsuri.expensereports.utils.format
import com.ramitsuri.expensereports.utils.formattedSuspend
import java.math.BigDecimal

data class ReportForTable(
    val name: String,
    val headerColumn: List<String>,
    val headerRow: List<String>,
    val rows: List<List<String>>,
) {
    companion object {
        suspend fun fromReport(report: Report): ReportForTable {
            if (report.accounts.isEmpty()) {
                error("Report has no accounts")
            }
            val reportHasTotals = !report.withCumulativeBalance
            val accounts = report.accounts.sortedBy { it.order }

            val firstColumn =
                accounts.map {
                    val splits = it.name.split(":")
                    splits.last()
                }

            val sortedMonthYears =
                accounts.first() // Assume all accounts have the same number of month years
                    .monthTotals
                    .keys
                    .sorted()

            val headerRow =
                sortedMonthYears
                    .map { it.formattedSuspend() }
                    .let {
                        if (reportHasTotals) {
                            it.plus("Total")
                        } else {
                            it
                        }
                    }

            val rows =
                accounts.map { account ->
                    val monthTotals =
                        sortedMonthYears.map { monthYear ->
                            (account.monthTotals[monthYear] ?: BigDecimal.ZERO).format()
                        }
                    if (reportHasTotals) {
                        monthTotals.plus(account.monthTotals.sum().format())
                    } else {
                        monthTotals
                    }
                }

            return ReportForTable(
                name = report.name,
                headerColumn = firstColumn,
                headerRow = headerRow,
                rows = rows,
            )
        }
    }
}
