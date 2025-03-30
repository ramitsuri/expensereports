package com.ramitsuri.expensereports.model

sealed interface ReportNames {
    val name: String

    data object NetWorth : ReportNames {
        override val name: String = "NetWorth"
    }

    data object Income : ReportNames {
        override val name: String = "Income"
    }

    data object IncomeWithoutGains : ReportNames {
        override val name: String = "Income Without Gains"
    }

    data object Expenses : ReportNames {
        override val name: String = "Expenses"
    }

    data object AfterDeductionsExpenses : ReportNames {
        override val name: String = "After Deduction Expenses"
    }

    data object Assets : ReportNames {
        override val name: String = "Assets"
    }

    data object Liabilities : ReportNames {
        override val name: String = "Liabilities"
    }

    data object SavingsRate : ReportNames {
        override val name: String = "Savings Rate"
    }

    data object Savings : ReportNames {
        override val name: String = "Savings"
    }
}
