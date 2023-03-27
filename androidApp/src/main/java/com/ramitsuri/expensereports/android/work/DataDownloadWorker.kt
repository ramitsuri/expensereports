package com.ramitsuri.expensereports.android.work

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.ramitsuri.expensereports.android.MainApplication
import com.ramitsuri.expensereports.utils.LogHelper
import com.ramitsuri.expensereports.utils.DataDownloader
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.time.Duration

class DataDownloadWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams), KoinComponent {

    private val downloader: DataDownloader by inject()

    override suspend fun doWork(): Result {
        val app = applicationContext as? MainApplication ?: return Result.failure()

        if (app.isInForeground) {
            LogHelper.v(TAG, "App in foreground will retry downloading reports")
            return Result.retry()
        }

        downloader.download()

        return Result.success()
    }

    companion object {
        private const val TAG = "ReportDownloadWorker"
        private const val WORK_NAME = "PeriodicDownloadWorker"
        private const val REPEAT_HOURS = 6L

        fun enqueuePeriodic(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiresCharging(true)
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = PeriodicWorkRequestBuilder<DataDownloadWorker>(
                repeatInterval = Duration.ofHours(REPEAT_HOURS)
            )
                .addTag(TAG)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    WORK_NAME,
                    ExistingPeriodicWorkPolicy.REPLACE,
                    request
                )
        }
    }
}