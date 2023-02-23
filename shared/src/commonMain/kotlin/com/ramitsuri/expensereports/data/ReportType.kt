package com.ramitsuri.expensereports.data

enum class ReportType(val key: Long, val reportName: String, val hasTotal: Boolean) {
    NONE(
        key = 0,
        reportName = "None",
        hasTotal = false
    ),

    EXPENSE(
        key = 1,
        reportName = "Expenses",
        hasTotal = true
    ),

    EXPENSE_AFTER_DEDUCTION(
        key = 2,
        reportName = "After_Deduction_Expenses",
        hasTotal = true
    ),

    ASSETS(
        key = 3,
        reportName = "Assets",
        hasTotal = false
    ),

    LIABILITIES(
        key = 4,
        reportName = "Liabilities",
        hasTotal = false
    ),

    INCOME(
        key = 5,
        reportName = "Income",
        hasTotal = true
    ),

    NET_WORTH(
        key = 6,
        reportName = "NetWorth",
        hasTotal = false
    ),

    SAVINGS(
        key = 7,
        reportName = "Savings",
        hasTotal = true
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