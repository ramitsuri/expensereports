package com.ramitsuri.expensereports.di

import com.ramitsuri.expensereports.data.db.Database
import com.ramitsuri.expensereports.data.prefs.PrefManager
import com.ramitsuri.expensereports.data.prefs.SettingsKeyValueStore
import com.ramitsuri.expensereports.db.ReportsDb
import com.ramitsuri.expensereports.utils.DispatcherProvider
import com.russhwolf.settings.NSUserDefaultsSettings
import com.squareup.sqldelight.drivers.native.NativeSqliteDriver
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import kotlinx.serialization.json.Json
import org.koin.dsl.module
import platform.Foundation.NSUserDefaults

actual val platformModule = module {
    single<HttpClientEngine> { Darwin.create() }

    single<DispatcherProvider> {
        DispatcherProvider()
    }

    single<PrefManager> {
        val keyValueStore =
            SettingsKeyValueStore(NSUserDefaultsSettings(NSUserDefaults.standardUserDefaults))
        PrefManager(keyValueStore)
    }

    single<Database> {
        Database(
            NativeSqliteDriver(
                ReportsDb.Schema,
                "chores.db"
            ),
            get<DispatcherProvider>(),
            get<Json>()
        )
    }
}