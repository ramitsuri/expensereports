package com.ramitsuri.expensereports.di

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.ramitsuri.expensereports.data.db.Database
import com.ramitsuri.expensereports.data.prefs.PrefManager
import com.ramitsuri.expensereports.data.prefs.SettingsKeyValueStore
import com.ramitsuri.expensereports.data.prefs.Store
import com.ramitsuri.expensereports.db.ReportsDb
import com.ramitsuri.expensereports.repository.MiscellaneousRepository
import com.ramitsuri.expensereports.repository.ReportsRepository
import com.ramitsuri.expensereports.utils.Constants
import com.ramitsuri.expensereports.utils.DataDownloader
import com.ramitsuri.expensereports.utils.DispatcherProvider
import com.ramitsuri.expensereports.utils.Logger
import com.ramitsuri.expensereports.viewmodel.HomeCallbackViewModel
import com.ramitsuri.expensereports.viewmodel.SettingsCallbackViewModel
import com.russhwolf.settings.NSUserDefaultsSettings
import com.squareup.sqldelight.drivers.native.NativeSqliteDriver
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import okio.Path.Companion.toPath
import org.koin.core.KoinApplication
import org.koin.core.component.KoinComponent
import org.koin.dsl.module
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDefaults
import platform.Foundation.NSUserDomainMask

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
                "reports.db"
            ),
            get<DispatcherProvider>(),
            get<Json>()
        )
    }

    single<Store> {
        val producePath = {
            val documentDirectory: NSURL? = NSFileManager.defaultManager.URLForDirectory(
                directory = NSDocumentDirectory,
                inDomain = NSUserDomainMask,
                appropriateForURL = null,
                create = false,
                error = null,
            )
            requireNotNull(documentDirectory).path + "/${Constants.STORE_FILE}"
        }
        Store(
        PreferenceDataStoreFactory.createWithPath(produceFile = { producePath().toPath() }))
    }
}

fun initKoinIos(
    appInfo: AppInfo,
): KoinApplication = initKoin(
    module {
        factory<AppInfo> {
            appInfo
        }

        single<HomeCallbackViewModel> {
            HomeCallbackViewModel(
                get<ReportsRepository>(),
                get<MiscellaneousRepository>(),
                get<Clock>(),
                get<DispatcherProvider>()
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
    fun getHomeViewModel() = getKoin().get<HomeCallbackViewModel>()

    fun getSettingsViewModel() = getKoin().get<SettingsCallbackViewModel>()

    fun getDownloader() = getKoin().get<DataDownloader>()

    fun getLogger() = getKoin().get<Logger>()
}