package com.ramitsuri.expensereports.testutils

import com.ramitsuri.expensereports.notification.NotificationHandler
import com.ramitsuri.expensereports.notification.NotificationInfo
import com.ramitsuri.expensereports.notification.NotificationType

class TestNotificationHandler : NotificationHandler {
    var shownNotification: NotificationInfo? = null

    override fun showNotification(notificationInfo: NotificationInfo) {
        shownNotification = notificationInfo
    }

    override fun registerTypes(types: List<NotificationType>) {
        println("Registered types")
    }
}
