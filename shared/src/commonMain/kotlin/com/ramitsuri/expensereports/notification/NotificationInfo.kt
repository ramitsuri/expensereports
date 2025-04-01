package com.ramitsuri.expensereports.notification

data class NotificationInfo(
    val type: NotificationType,
    val title: String,
    val body: String,
)
