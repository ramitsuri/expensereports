package com.ramitsuri.expensereports.notification

import com.ramitsuri.expensereports.database.dao.ReportsDao
import com.ramitsuri.expensereports.model.MonthYear
import com.ramitsuri.expensereports.model.Report
import com.ramitsuri.expensereports.model.ReportNames
import com.ramitsuri.expensereports.settings.Settings
import com.ramitsuri.expensereports.testutils.BaseTest
import com.ramitsuri.expensereports.usecase.SavingsRateUseCase
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.junit.Test
import org.koin.core.component.get
import org.koin.core.component.inject
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class MonthEndIncomeExpenseNotificationHelperTest : BaseTest() {
    private val settings by inject<Settings>()
    private val reportDao by inject<ReportsDao>()
    private val lastMonth = MonthYear(Month.JANUARY, 2025)
    private val timeZone = TimeZone.of("America/Los_Angeles")
    private lateinit var savingsRateUseCase: SavingsRateUseCase
    private lateinit var clock: TestClock
    private lateinit var notificationHandler: TestNotificationHandler
    private lateinit var helper: MonthEndIncomeExpenseNotificationHelper

    @Test
    fun `should not show if last shown this month`() =
        runTest {
            initHelper()
            insertSavingsRateReportForLastMonth()
            clock.nowLocal = "2025-02-03T12:00:00"
            setLastShownTime("2025-02-01T18:00:00")

            helper.show()

            assertNull(notificationHandler.shownNotification)
        }

    @Test
    fun `should not show if day of month less than 3`() =
        runTest {
            initHelper()
            insertSavingsRateReportForLastMonth()
            clock.nowLocal = "2025-02-02T12:00:00"

            helper.show()

            assertNull(notificationHandler.shownNotification)
        }

    @Test
    fun `should not show if no savings rate report`() =
        runTest {
            initHelper()
            clock.nowLocal = "2025-02-05T12:00:00"
            setLastShownTime("2025-01-02T18:00:00")

            helper.show()

            assertNull(notificationHandler.shownNotification)
        }

    @Test
    fun `should show if never shown before`() =
        runTest {
            initHelper()
            insertSavingsRateReportForLastMonth()
            clock.nowLocal = "2025-02-05T12:00:00"

            helper.show()

            assertNotNull(notificationHandler.shownNotification)
        }

    @Test
    fun `should save last shown time if showing`() =
        runTest {
            initHelper()
            insertSavingsRateReportForLastMonth()
            clock.nowLocal = "2025-02-05T12:00:00"

            helper.show()

            assertEquals(clock.now(), settings.getLastMonthEndIncomeExpensesNotification())
        }

    @Test
    fun `notification content should match if showing - 1`() =
        runTest {
            initHelper()
            insertSavingsRateReportForLastMonth()
            clock.nowLocal = "2025-02-05T12:00:00"

            helper.show()

            assertEquals(
                NotificationInfo(
                    type = NotificationType.MonthEndIncomeExpenses,
                    title = "Incomes & Expenses last month",
                    body =
                        "You earned $25 and spent $10 last month. " +
                            "You saved 50% of the earned amount.",
                ),
                notificationHandler.shownNotification,
            )
        }

    @Test
    fun `notification content should match if showing - 2`() =
        runTest {
            initHelper()
            insertSavingsRateReportForLastMonth(
                income = BigDecimal("24"),
                expenses = BigDecimal("12"),
                taxes = BigDecimal("4"),
            )
            clock.nowLocal = "2025-02-05T12:00:00"

            helper.show()

            assertEquals(
                NotificationInfo(
                    type = NotificationType.MonthEndIncomeExpenses,
                    title = "Incomes & Expenses last month",
                    body =
                        "You earned $24 and spent $12 last month. " +
                            "You saved 40% of the earned amount.",
                ),
                notificationHandler.shownNotification,
            )
        }

    private fun initHelper() {
        clock = TestClock(timeZone)
        notificationHandler = TestNotificationHandler()
        savingsRateUseCase =
            SavingsRateUseCase(
                mainRepository = get(),
                clock = clock,
                timeZone = timeZone,
            )
        helper =
            MonthEndIncomeExpenseNotificationHelper(
                savingsRateUseCase = savingsRateUseCase,
                settings = settings,
                notificationHandler = notificationHandler,
                clock = clock,
                timeZone = timeZone,
            )
    }

    private suspend fun setLastShownTime(localDateTime: String) {
        val instant = LocalDateTime.parse(localDateTime).toInstant(timeZone)
        settings.setLastMonthEndIncomeExpensesNotification(instant)
    }

    private class TestClock(val timeZone: TimeZone) : Clock {
        var nowLocal = Instant.DISTANT_PAST.toLocalDateTime(timeZone).toString()

        override fun now(): Instant {
            return LocalDateTime.parse(nowLocal).toInstant(timeZone)
        }
    }

    private class TestNotificationHandler : NotificationHandler {
        var shownNotification: NotificationInfo? = null

        override fun showNotification(notificationInfo: NotificationInfo) {
            shownNotification = notificationInfo
        }

        override fun registerTypes(types: List<NotificationType>) {
            println("Registered types")
        }
    }

    private suspend fun insertSavingsRateReportForLastMonth(
        income: BigDecimal = BigDecimal("25"),
        expenses: BigDecimal = BigDecimal("10"),
        taxes: BigDecimal = BigDecimal("5"),
    ) {
        reportDao.insert(
            listOf(
                Report(
                    name = ReportNames.SavingsRate.name,
                    withCumulativeBalance = false,
                    accounts =
                        listOf(
                            Report.Account(
                                name = "Income",
                                order = 0,
                                monthTotals =
                                    mapOf(
                                        lastMonth to income,
                                    ),
                            ),
                            Report.Account(
                                name = "Taxes",
                                order = 1,
                                monthTotals =
                                    mapOf(
                                        lastMonth to taxes,
                                    ),
                            ),
                            Report.Account(
                                name = "Expenses",
                                order = 2,
                                monthTotals =
                                    mapOf(
                                        lastMonth to expenses,
                                    ),
                            ),
                        ),
                ),
            ),
        )
    }
}
