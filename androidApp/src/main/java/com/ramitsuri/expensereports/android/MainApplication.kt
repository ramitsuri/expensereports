package com.ramitsuri.expensereports.android

import android.app.Application
import android.content.Context
import android.os.Build
import com.google.android.material.color.DynamicColors
import com.ramitsuri.expensereports.data.prefs.PrefManager
import com.ramitsuri.expensereports.di.AppInfo
import com.ramitsuri.expensereports.di.initKoin
import com.ramitsuri.expensereports.repository.ReportsRepository
import com.ramitsuri.expensereports.utils.DispatcherProvider
import com.ramitsuri.expensereports.viewmodel.ExpenseReportViewModel
import com.ramitsuri.expensereports.viewmodel.SettingsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)
        initDependencyInjection()
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