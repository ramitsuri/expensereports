package com.ramitsuri.expensereports.android

import android.app.Application
import android.content.Context
import android.os.Build
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.material.color.DynamicColors
import com.ramitsuri.expensereports.android.work.ReportDownloadWorker
import com.ramitsuri.expensereports.data.prefs.PrefManager
import com.ramitsuri.expensereports.di.AppInfo
import com.ramitsuri.expensereports.di.initKoin
import com.ramitsuri.expensereports.repository.ReportsRepository
import com.ramitsuri.expensereports.utils.DispatcherProvider
import com.ramitsuri.expensereports.viewmodel.ExpenseReportViewModel
import com.ramitsuri.expensereports.viewmodel.HomeViewModel
import com.ramitsuri.expensereports.viewmodel.SettingsViewModel
import kotlinx.datetime.Clock
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
        ReportDownloadWorker.enqueuePeriodic(this)
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
                    ExpenseReportViewModel(
                        get<ReportsRepository>(),
                        get<DispatcherProvider>(),
                        get<PrefManager>()
                    )
                }

                viewModel {
                    HomeViewModel(
                        get<ReportsRepository>(),
                        get<Clock>()
                    )
                }

                viewModel {
                    SettingsViewModel(
                        get<PrefManager>()
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