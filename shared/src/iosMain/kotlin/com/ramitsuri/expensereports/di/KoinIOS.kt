package com.ramitsuri.expensereports.di

import com.ramitsuri.expensereports.data.db.Database
import com.ramitsuri.expensereports.data.prefs.PrefManager
import com.ramitsuri.expensereports.data.prefs.SettingsKeyValueStore
import com.ramitsuri.expensereports.db.ReportsDb
import com.ramitsuri.expensereports.repository.TransactionsRepository
import com.ramitsuri.expensereports.utils.DataDownloader
import com.ramitsuri.expensereports.utils.DispatcherProvider
import com.ramitsuri.expensereports.utils.Logger
import com.ramitsuri.expensereports.viewmodel.SettingsCallbackViewModel
import com.ramitsuri.expensereports.viewmodel.TransactionsCallbackViewModel
import com.russhwolf.settings.NSUserDefaultsSettings
import com.squareup.sqldelight.drivers.native.NativeSqliteDriver
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.serialization.json.Json
import org.koin.core.KoinApplication
import org.koin.core.component.KoinComponent
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

    single<Logger> {
        Logger(get<AppInfo>().isDebug)
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

fun initKoinIos(
    appInfo: AppInfo,
): KoinApplication = initKoin(
    module {
        factory<AppInfo> {
            appInfo
        }

        single<TransactionsCallbackViewModel> {
            TransactionsCallbackViewModel(
                get<TransactionsRepository>(),
                get<DispatcherProvider>(),
                get<Clock>(),
                get<TimeZone>(),
            )
        }

        single<SettingsCallbackViewModel> {
            SettingsCallbackViewModel(
                get<PrefManager>(),
                get<DataDownloader>()
            )
        }
    }
)

// To be called from Swift code
object Dependencies : KoinComponent {
    fun getTransactionsViewModel() = getKoin().get<TransactionsCallbackViewModel>()

    fun getSettingsViewModel() = getKoin().get<SettingsCallbackViewModel>()

    fun getDownloader() = getKoin().get<DataDownloader>()

    fun getLogger() = getKoin().get<Logger>()
}