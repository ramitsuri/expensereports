package com.ramitsuri.expensereports.di

import com.ramitsuri.expensereports.data.prefs.PrefManager
import com.ramitsuri.expensereports.data.prefs.SettingsKeyValueStore
import com.ramitsuri.expensereports.utils.DispatcherProvider
import com.russhwolf.settings.NSUserDefaultsSettings
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
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
}