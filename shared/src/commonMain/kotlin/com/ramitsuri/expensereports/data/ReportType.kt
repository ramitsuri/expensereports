package com.ramitsuri.expensereports.data

enum class ReportType(val key: Long, val reportName: String, val hasRunningTotal: Boolean= false) {
    NONE(
        key = 0,
        reportName = "None"
    ),

    EXPENSE(
        key = 1,
        reportName = "Expenses"
    ),

    EXPENSE_AFTER_DEDUCTION(
        key = 2,
        reportName = "After_Deduction_Expenses"
    ),

    ASSETS(
        key = 3,
        reportName = "Assets",
        hasRunningTotal = true
    ),

    LIABILITIES(
        key = 4,
        reportName = "Liabilities",
        hasRunningTotal = true
    ),

    INCOME(
        key = 5,
        reportName = "Income"
    ),

    NET_WORTH(
        key = 6,
        reportName = "NetWorth",
        hasRunningTotal = true
    ),

    SAVINGS(
        key = 7,
        reportName = "Savings"
    );

    companion object {
        fun fromKey(key: Long): ReportType {
            for (value in values()) {
                if (value.key == key) {
                    return value
                }
            }
            return NONE
        }
    }
}