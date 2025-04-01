package com.ramitsuri.expensereports.notification

sealed interface NotificationType {
    data object MonthEndIncomeExpenses : NotificationType
}
