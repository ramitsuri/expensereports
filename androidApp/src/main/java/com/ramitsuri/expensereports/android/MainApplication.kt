package com.ramitsuri.expensereports.android

import android.app.Application
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.material.color.DynamicColors
import com.ramitsuri.expensereports.android.work.DataDownloadWorker
import com.ramitsuri.expensereports.initSdk
import com.ramitsuri.expensereports.notification.ShowNotificationWorker
import org.koin.core.component.KoinComponent

class MainApplication : Application(), LifecycleEventObserver, KoinComponent {
    var isInForeground: Boolean = false
        private set

    override fun onCreate() {
        super.onCreate()
        initSdk(this)
        DynamicColors.applyToActivitiesIfAvailable(this)

        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        enqueueWorkers()
    }

    override fun onStateChanged(
        source: LifecycleOwner,
        event: Lifecycle.Event,
    ) {
        if (event == Lifecycle.Event.ON_START) {
            isInForeground = true
        }
        if (event == Lifecycle.Event.ON_STOP) {
            isInForeground = false
        }
    }

    private fun enqueueWorkers() {
        DataDownloadWorker.enqueuePeriodic(this)
        ShowNotificationWorker.enqueue(this)
    }
}
