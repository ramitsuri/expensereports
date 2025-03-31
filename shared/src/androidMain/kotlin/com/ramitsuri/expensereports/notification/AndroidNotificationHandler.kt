package com.ramitsuri.expensereports.notification

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager.IMPORTANCE_DEFAULT
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import androidx.annotation.StringRes
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import androidx.core.net.toUri
import com.ramitsuri.expensereports.R
import com.ramitsuri.expensereports.log.logI
import com.ramitsuri.expensereports.log.logW
import com.ramitsuri.expensereports.ui.navigation.Destination

internal class AndroidNotificationHandler(
    context: Context
) : NotificationHandler {

    private val appContext = context.applicationContext
    private val notificationManager = NotificationManagerCompat.from(appContext)

    override fun registerTypes(types: List<NotificationType>) {
        createChannels(types)
    }

    override fun showNotification(notificationInfo: NotificationInfo) {
        if (ActivityCompat.checkSelfPermission(
                appContext,
                Manifest.permission.POST_NOTIFICATIONS,
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            logW(TAG) { "Notification permission not granted" }
            return
        }
        notificationInfo.let {
            logI(TAG) { "Showing notification $notificationInfo" }
            notificationManager.notify(it.type.notificationId(), it.toNotification())
        }
    }

    private fun NotificationInfo.toNotification(): Notification {
        val channel = type.toChannel()
        val builder = NotificationCompat.Builder(appContext, channel.id)
        builder.apply {
            setSmallIcon(R.drawable.ic_notification)
            setVisibility(type.visibility())
            setContentTitle(title)
            setContentText(body)
            if (type.isOngoing()) {
                setOngoing(true)
                setShowWhen(false)
                setWhen(0)
            } else {
                setOngoing(false)
            }
            setAutoCancel(type.cancelOnTouch())
            val contentIntent =
                TaskStackBuilder.create(appContext).run {
                    val mainIntent =
                        Intent(
                            Intent.ACTION_VIEW,
                            Destination.Home.deepLinkUri.toUri(),
                        )
                    addNextIntentWithParentStack(mainIntent)
                    val flags = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                    getPendingIntent(type.notificationId(), flags)
                }
            setContentIntent(contentIntent)
            setGroup(channel.id)
        }
        return builder.build()
    }

    private fun createChannels(types: List<NotificationType>) {
        types
            .toChannels()
            .map { channel ->
                NotificationChannel(
                    /* id = */ channel.id,
                    /* name = */ appContext.getString(channel.title),
                    /* importance = */ channel.importance,
                ).apply {
                    if (importance >= IMPORTANCE_DEFAULT) {
                        // Sound
                        val attributes =
                            AudioAttributes.Builder()
                                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                                .build()
                        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                        setSound(soundUri, attributes)

                        // Vibration
                        vibrationPattern = longArrayOf(200)

                        setShowBadge(true)
                    }
                }
            }
            .forEach { channel ->
                notificationManager.createNotificationChannel(channel)
            }

    }

    private fun List<NotificationType>.toChannels(): List<Channel> {
        return map { it.toChannel() }
            .distinctBy { it.id }
    }

    private fun NotificationType.toChannel(): Channel {
        return when (this) {
            is NotificationType.MonthEndIncomeExpenses -> Channel.GENERAL
        }
    }

    private fun NotificationType.visibility(): Int {
        return when (this) {
            is NotificationType.MonthEndIncomeExpenses -> NotificationCompat.VISIBILITY_PRIVATE
        }
    }

    private fun NotificationType.isOngoing(): Boolean {
        return when (this) {
            is NotificationType.MonthEndIncomeExpenses -> false
        }
    }

    private fun NotificationType.cancelOnTouch(): Boolean {
        return when (this) {
            is NotificationType.MonthEndIncomeExpenses -> true
        }
    }

    private fun NotificationType.notificationId(): Int {
        return when (this) {
            is NotificationType.MonthEndIncomeExpenses -> 1
        }
    }

    private enum class Channel(
        val id: String,
        @StringRes
        val title: Int,
        val importance: Int,
    ) {
        GENERAL(
            id = "general",
            title = R.string.channel_general_name,
            importance = IMPORTANCE_DEFAULT,
        ),
    }

    companion object {
        private const val TAG = "AndroidNotificationHandler"
    }
}
