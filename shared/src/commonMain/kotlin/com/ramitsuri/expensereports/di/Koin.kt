package com.ramitsuri.expensereports.di

import com.ramitsuri.expensereports.data.db.Database
import com.ramitsuri.expensereports.data.db.ReportDao
import com.ramitsuri.expensereports.data.db.TransactionsDao
import com.ramitsuri.expensereports.data.prefs.PrefManager
import com.ramitsuri.expensereports.data.prefs.Store
import com.ramitsuri.expensereports.network.NetworkProvider
import com.ramitsuri.expensereports.repository.MiscellaneousRepository
import com.ramitsuri.expensereports.repository.ReportsRepository
import com.ramitsuri.expensereports.repository.TransactionsRepository
import com.ramitsuri.expensereports.utils.DataDownloader
import com.ramitsuri.expensereports.utils.DispatcherProvider
import io.ktor.client.engine.HttpClientEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.serialization.json.Json
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module

fun initKoin(appModule: Module): KoinApplication {
    val koinApplication = startKoin {
        modules(
            appModule,
            platformModule,
            coreModule
        )
    }
    return koinApplication
}

private val coreModule = module {
    single<NetworkProvider> {
        NetworkProvider(
            get<DispatcherProvider>(),
            get<PrefManager>(),
            get<Json>(),
            get<HttpClientEngine>()
        )
    }

    factory<ReportsRepository> {
        ReportsRepository(
            get<NetworkProvider>().reportApi(),
            get<ReportDao>(),
            get<Clock>()
        )
    }

    factory<TransactionsRepository> {
        TransactionsRepository(
            get<TransactionsDao>(),
        )
    }

    factory<MiscellaneousRepository> {
        MiscellaneousRepository(
            get<NetworkProvider>().miscellaneousApi(),
            get<Json>(),
            get<Store>()
        )
    }

    factory<DataDownloader> {
        DataDownloader(
            get<ReportDao>(),
            get<NetworkProvider>().reportApi(),
            get<TransactionsDao>(),
            get<NetworkProvider>().transactionsApi(),
            get<NetworkProvider>().transactionGroupsApi(),
            get<MiscellaneousRepository>(),
            get<Clock>(),
            get<TimeZone>(),
            get<PrefManager>()
        )
    }

    single<CoroutineScope> {
        CoroutineScope(SupervisorJob())
    }

    factory<ReportDao> {
        get<Database>().reportDao
    }

    factory<TransactionsDao> {
        get<Database>().transactionsDao
    }

    single<Json> {
        Json {
            isLenient = true
            ignoreUnknownKeys = true
        }
    }

    single<Clock> {
        Clock.System
    }

    single<TimeZone> {
        TimeZone.currentSystemDefault()
    }
}

expect val platformModule: Module

interface AppInfo {
    val appId: String
    val isDebug: Boolean
    val deviceDetails: String
}