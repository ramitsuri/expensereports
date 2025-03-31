package com.ramitsuri.expensereports.notification

interface NotificationHandler {
    fun registerTypes(types: List<NotificationType>)
    fun showNotification(notificationInfo: NotificationInfo)
}
