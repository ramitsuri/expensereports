package com.ramitsuri.expensereports.notification

import com.ramitsuri.expensereports.log.logI
import com.ramitsuri.expensereports.log.logW
import com.ramitsuri.expensereports.model.Period
import com.ramitsuri.expensereports.settings.Settings
import com.ramitsuri.expensereports.usecase.SavingsRateUseCase
import com.ramitsuri.expensereports.utils.format
import com.ramitsuri.expensereports.utils.formatPercent
import com.ramitsuri.expensereports.utils.nowLocal
import expensereports.shared.generated.resources.Res
import expensereports.shared.generated.resources.income_expense_notification_body
import expensereports.shared.generated.resources.income_expense_notification_title
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.getString

class MonthEndIncomeExpenseNotificationHelper(
    private val savingsRateUseCase: SavingsRateUseCase,
    private val notificationHandler: NotificationHandler,
    private val settings: Settings,
    private val clock: Clock,
    private val timeZone: TimeZone,
) {
    suspend fun show() {
        if (shownThisMonthAlready()) {
            return
        }
        if (clock.nowLocal(timeZone).dayOfMonth < 3) {
            // To give enough time to enter data into gnucash
            logI(TAG) { "Not showing yet because day of month < 3" }
            return
        }
        logI(TAG) { "Can show month end income expenses notification" }

        val savingsRates = savingsRateUseCase(listOf(Period.PreviousMonth)).first()
        if (savingsRates.isEmpty()) {
            logW(TAG) { "SavingsRates is empty" }
            return
        }
        val savingsRateLastMonth = savingsRates.getValue(Period.PreviousMonth)
        val expenses = savingsRateLastMonth.expenses
        val income = savingsRateLastMonth.income

        val notification =
            NotificationInfo(
                type = NotificationType.MonthEndIncomeExpenses,
                title = getString(Res.string.income_expense_notification_title),
                body =
                    getString(
                        Res.string.income_expense_notification_body,
                        income.format(),
                        expenses.format(),
                        savingsRateLastMonth.savingsRate.formatPercent(),
                    ),
            )
        notificationHandler.showNotification(notification)
        settings.setLastMonthEndIncomeExpensesNotification(clock.now())
    }

    private suspend fun shownThisMonthAlready(): Boolean {
        val lastNotificationDateTime =
            settings
                .getLastMonthEndIncomeExpensesNotification()
                ?.toLocalDateTime(timeZone)
        val nowLocal = clock.nowLocal(timeZone)
        if (lastNotificationDateTime == null) {
            logI(TAG) { "No last notification date time" }
            return false
        }
        if (lastNotificationDateTime.year != nowLocal.year &&
            lastNotificationDateTime.month != nowLocal.month
        ) {
            logI(TAG) { "Last notification date time not this month" }
            return false
        }
        return true
    }

    companion object {
        private const val TAG = "ShowNotificationHelper"
    }
}
