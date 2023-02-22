package com.ramitsuri.expensereports.di

import android.content.Context
import com.ramitsuri.expensereports.data.db.Database
import com.ramitsuri.expensereports.data.prefs.PrefManager
import com.ramitsuri.expensereports.data.prefs.SettingsKeyValueStore
import com.ramitsuri.expensereports.db.ReportsDb
import com.ramitsuri.expensereports.utils.DispatcherProvider
import com.ramitsuri.expensereports.utils.Logger
import com.russhwolf.settings.SharedPreferencesSettings
import com.squareup.sqldelight.android.AndroidSqliteDriver
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import kotlinx.serialization.json.Json
import org.koin.dsl.module

actual val platformModule = module {
    factory<HttpClientEngine> {
        OkHttp.create()
    }

    single<DispatcherProvider> {
        DispatcherProvider()
    }

    single<PrefManager> {
        val context: Context = get()
        val fileName = "com.ramitsuri.expensereports.android.prefs"

        val sharedPrefs =
            context.applicationContext.getSharedPreferences(fileName, Context.MODE_PRIVATE)
        val keyValueStore = SettingsKeyValueStore(SharedPreferencesSettings(sharedPrefs))

        PrefManager(keyValueStore)
    }

    single<Logger> {
        Logger(
            get<AppInfo>().isDebug,
            get<AppInfo>().deviceDetails
        )
    }

    single<Database> {
        Database(
            AndroidSqliteDriver(
                ReportsDb.Schema,
                get<Context>(),
                "reports.db"
            ),
            get<DispatcherProvider>(),
            get<Json>()
        )
    }
}