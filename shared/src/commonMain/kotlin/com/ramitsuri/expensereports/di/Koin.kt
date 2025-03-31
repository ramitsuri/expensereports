package com.ramitsuri.expensereports.di

import androidx.room.RoomDatabase
import com.ramitsuri.expensereports.database.AppDatabase
import com.ramitsuri.expensereports.database.dao.CurrentBalancesDao
import com.ramitsuri.expensereports.database.dao.ReportsDao
import com.ramitsuri.expensereports.database.dao.TransactionsDao
import com.ramitsuri.expensereports.log.logI
import com.ramitsuri.expensereports.network.api.Api
import com.ramitsuri.expensereports.network.api.impl.ApiImpl
import com.ramitsuri.expensereports.repository.MainRepository
import com.ramitsuri.expensereports.settings.DataStoreKeyValueStore
import com.ramitsuri.expensereports.settings.Settings
import com.ramitsuri.expensereports.shared.BuildKonfig
import com.ramitsuri.expensereports.ui.home.HomeViewModel
import com.ramitsuri.expensereports.ui.settings.SettingsViewModel
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json
import okio.Path
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

fun initKoin(appModule: KoinApplication.() -> Module): KoinApplication {
    val koinApplication =
        startKoin {
            modules(
                appModule(),
                coreModule,
            )
        }
    return koinApplication
}

internal val coreModule =
    module {
        single<Settings> {
            val dataStore = DataStoreKeyValueStore { get<Path>() }
            Settings(
                keyValueStore = dataStore,
            )
        }

        single<AppDatabase> {
            val ioDispatcher = get<CoroutineDispatcher>(qualifier = KoinQualifier.IO_DISPATCHER)
            AppDatabase.getDb(
                builder = get<RoomDatabase.Builder<AppDatabase>>(),
                dispatcher = ioDispatcher,
                json = get<Json>(),
            )
        }

        single<CoroutineDispatcher>(qualifier = KoinQualifier.IO_DISPATCHER) {
            Dispatchers.IO
        }

        single<HttpClient> {
            HttpClient(get<HttpClientEngine>()) {
                install(ContentNegotiation) {
                    json(get<Json>())
                }
                install(Logging) {
                    logger =
                        object : Logger {
                            override fun log(message: String) {
                                logI("HTTP") { message }
                            }
                        }
                    level =
                        if (get<Boolean>(qualifier = KoinQualifier.IS_DEBUG)) {
                            LogLevel.ALL
                        } else {
                            LogLevel.HEADERS
                        }
                }
            }
        }

        single<Json> {
            Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
                allowStructuredMapKeys = true
            }
        }

        single<Api> {
            ApiImpl(
                httpClient = get<HttpClient>(),
                ioDispatcher = get<CoroutineDispatcher>(qualifier = KoinQualifier.IO_DISPATCHER),
            )
        }

        single<MainRepository> {
            MainRepository(
                transactionsDao = get<TransactionsDao>(),
                reportsDao = get<ReportsDao>(),
                currentBalancesDao = get<CurrentBalancesDao>(),
                api = get<Api>(),
                settings = get<Settings>(),
            )
        }

        factory<String>(qualifier = KoinQualifier.DATABASE_NAME) {
            "app_database"
        }

        factory<String>(qualifier = KoinQualifier.DATASTORE_FILE_NAME) {
            "expense_reports.preferences_pb"
        }

        factory<TransactionsDao> {
            get<AppDatabase>().transactionsDao()
        }

        factory<ReportsDao> {
            get<AppDatabase>().reportsDao()
        }

        factory<CurrentBalancesDao> {
            get<AppDatabase>().currentBalancesDao()
        }

        factory<Boolean>(qualifier = KoinQualifier.IS_DEBUG) {
            BuildKonfig.IS_DEBUG
        }

        viewModel<HomeViewModel> {
            HomeViewModel(
                mainRepository = get<MainRepository>(),
                isDesktop = get<Boolean>(qualifier = KoinQualifier.IS_DESKTOP),
            )
        }

        viewModel<SettingsViewModel> {
            SettingsViewModel(
                settings = get<Settings>(),
            )
        }
    }

object KoinQualifier {
    val IO_DISPATCHER = named("io_dispatcher")
    val IS_DEBUG = named("is_debug")
    val DATASTORE_FILE_NAME = named("datastore_file_name")
    val DATABASE_NAME = named("database_name")
    val IS_DESKTOP = named("is_desktop")
}
