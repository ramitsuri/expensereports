package com.ramitsuri.expensereports.testutils

import androidx.room.Room
import androidx.room.RoomDatabase
import com.ramitsuri.expensereports.database.AppDatabase
import com.ramitsuri.expensereports.di.KoinQualifier
import com.ramitsuri.expensereports.log.logI
import com.ramitsuri.expensereports.notification.NotificationHandler
import com.ramitsuri.expensereports.notification.NotificationInfo
import com.ramitsuri.expensereports.notification.NotificationType
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.java.Java
import okio.Path
import okio.Path.Companion.toOkioPath
import org.koin.dsl.module
import java.nio.file.Paths
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
val testModule =
    module {
        factory<HttpClientEngine> {
            HttpClient(Java)
            Java.create()
        }

        factory<RoomDatabase.Builder<AppDatabase>> {
            Room.inMemoryDatabaseBuilder<AppDatabase>()
        }

        factory<Path> {
            Paths.get(BaseTest.TEMP_DIR).resolve("${Uuid.random()}.preferences_pb").toOkioPath()
        }

        factory<Boolean>(qualifier = KoinQualifier.IS_DESKTOP) {
            true
        }

        single<NotificationHandler> {
            object : NotificationHandler {
                override fun registerTypes(types: List<NotificationType>) {
                    logI("TestNotificationHandler") {
                        "Registering notifications $types"
                    }
                }

                override fun showNotification(notificationInfo: NotificationInfo) {
                    logI("TestNotificationHandler") {
                        "Showing notification $notificationInfo"
                    }
                }
            }
        }
    }
