package com.ramitsuri.expensereports.android

import android.app.Application
import android.content.Context
import android.os.Build
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.material.color.DynamicColors
import com.ramitsuri.expensereports.android.work.DataDownloadWorker
import com.ramitsuri.expensereports.data.prefs.PrefManager
import com.ramitsuri.expensereports.di.AppInfo
import com.ramitsuri.expensereports.di.initKoin
import com.ramitsuri.expensereports.repository.MiscellaneousRepository
import com.ramitsuri.expensereports.repository.ReportsRepository
import com.ramitsuri.expensereports.repository.TransactionsRepository
import com.ramitsuri.expensereports.utils.DataDownloader
import com.ramitsuri.expensereports.viewmodel.ReportsViewModel
import com.ramitsuri.expensereports.viewmodel.HomeViewModel
import com.ramitsuri.expensereports.viewmodel.SettingsViewModel
import com.ramitsuri.expensereports.viewmodel.TransactionsViewModel
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

class MainApplication : Application(), LifecycleEventObserver {
    var isInForeground: Boolean = false
        private set

    override fun onCreate() {
        super.onCreate()
        initDependencyInjection()
        DynamicColors.applyToActivitiesIfAvailable(this)

        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        enqueueWorkers()
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (event == Lifecycle.Event.ON_START) {
            isInForeground = true
        }
        if (event == Lifecycle.Event.ON_STOP) {
            isInForeground = false
        }
    }

    private fun enqueueWorkers() {
        if (BuildConfig.DEBUG) {
            return
        }
        DataDownloadWorker.enqueuePeriodic(this)
    }

    private fun initDependencyInjection() {
        initKoin(
            appModule = module {
                single<Context> {
                    this@MainApplication
                }

                factory<AppInfo> {
                    AndroidAppInfo()
                }

                viewModel {
                    ReportsViewModel(
                        get<ReportsRepository>(),
                        get<DispatcherProvider>(),
                        get<Clock>(),
                        get<TimeZone>(),
                    )
                }

                viewModel {
                    HomeViewModel(
                        get<ReportsRepository>(),
                        get<MiscellaneousRepository>(),
                        get<PrefManager>(),
                        get<Clock>(),
                        get<DispatcherProvider>()
                    )
                }

                viewModel {
                    SettingsViewModel(
                        get<PrefManager>(),
                        get<DataDownloader>()
                    )
                }

                viewModel {
                    TransactionsViewModel(
                        get<TransactionsRepository>(),
                        get<DispatcherProvider>(),
                        get<Clock>(),
                        get<TimeZone>()
                    )
                }
            }
        )
    }
}

class AndroidAppInfo : AppInfo {
    override val appId: String = BuildConfig.APPLICATION_ID
    override val isDebug: Boolean = BuildConfig.DEBUG
    override val deviceDetails: String = Build.MODEL
}