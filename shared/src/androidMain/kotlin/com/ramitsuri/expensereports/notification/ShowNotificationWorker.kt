package com.ramitsuri.expensereports.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.ramitsuri.expensereports.log.logI
import com.ramitsuri.expensereports.utils.minus
import com.ramitsuri.expensereports.utils.nowLocal
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import java.util.concurrent.TimeUnit
import kotlin.time.toJavaDuration

class ShowNotificationWorker(
    private val monthEndIncomeExpenseNotificationHelper: MonthEndIncomeExpenseNotificationHelper,
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        logI(TAG) { "doWork" }
        monthEndIncomeExpenseNotificationHelper.show()
        return Result.success()
    }

    companion object {
        private const val TAG = "ShowNotificationWorker"
        private const val WORK_TAG = "TAG-ShowNotificationWorker"
        private const val WORK_NAME_PERIODIC = "ShowNotificationWorker"

        fun enqueue(
            context: Context,
            clock: Clock = Clock.System,
            timeZone: TimeZone = TimeZone.currentSystemDefault(),
        ) {
            val now = clock.nowLocal(timeZone)
            val showAt =
                if (now.hour > 18) {
                    LocalDateTime(now.date.plus(DatePeriod(days = 1)), LocalTime(hour = 8, minute = 0))
                } else {
                    now
                }
            val initialDelay = showAt.minus(now)
            PeriodicWorkRequest
                .Builder(ShowNotificationWorker::class.java, 24, TimeUnit.HOURS)
                .addTag(WORK_TAG)
                .addTag(WORK_NAME_PERIODIC)
                .setInitialDelay(initialDelay.toJavaDuration())
                .build()
                .let { request ->
                    WorkManager
                        .getInstance(context)
                        .enqueueUniquePeriodicWork(
                            WORK_NAME_PERIODIC,
                            ExistingPeriodicWorkPolicy.UPDATE,
                            request,
                        )
                }
        }
    }
}
