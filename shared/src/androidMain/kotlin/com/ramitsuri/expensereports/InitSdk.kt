package com.ramitsuri.expensereports

import android.app.Application
import androidx.room.Room
import androidx.room.RoomDatabase
import com.ramitsuri.expensereports.database.AppDatabase
import com.ramitsuri.expensereports.di.KoinQualifier
import com.ramitsuri.expensereports.di.initKoin
import com.ramitsuri.expensereports.notification.AndroidNotificationHandler
import com.ramitsuri.expensereports.notification.NotificationHandler
import com.ramitsuri.expensereports.notification.NotificationType
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.android.Android
import okio.Path
import okio.Path.Companion.toPath
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.dsl.module

fun initSdk(application: Application) {
    val app =
        initKoin {
            androidContext(application)
            module {
                workManagerFactory()

                factory<HttpClientEngine> {
                    Android.create()
                }

                factory<RoomDatabase.Builder<AppDatabase>> {
                    val dbName = get<String>(qualifier = KoinQualifier.DATABASE_NAME)
                    val dbFile = application.getDatabasePath(dbName)
                    Room.databaseBuilder(
                        application,
                        AppDatabase::class.java,
                        dbFile.absolutePath,
                    )
                }

                factory<Path> {
                    val fileName = get<String>(qualifier = KoinQualifier.DATASTORE_FILE_NAME)
                    application.filesDir.resolve(fileName).absolutePath.toPath()
                }

                factory<Boolean>(qualifier = KoinQualifier.IS_DESKTOP) {
                    false
                }

                single<NotificationHandler> {
                    AndroidNotificationHandler(
                        context = application,
                    )
                }
            }
        }
    val notificationHandler = app.koin.get<NotificationHandler>()

    notificationHandler.registerTypes(
        listOf(
            NotificationType.MonthEndIncomeExpenses,
        ),
    )
}
