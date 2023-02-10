package com.ramitsuri.expensereports.di

import com.ramitsuri.expensereports.data.prefs.PrefManager
import com.ramitsuri.expensereports.network.NetworkProvider
import com.ramitsuri.expensereports.repository.ReportsRepository
import com.ramitsuri.expensereports.utils.DispatcherProvider
import io.ktor.client.engine.HttpClientEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
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
            get<HttpClientEngine>()
        )
    }

    factory<ReportsRepository> {
        ReportsRepository(get<NetworkProvider>().reportApi())
    }

    single<CoroutineScope> {
        CoroutineScope(SupervisorJob())
    }
}

expect val platformModule: Module

interface AppInfo {
    val appId: String
    val isDebug: Boolean
    val deviceDetails: String
}