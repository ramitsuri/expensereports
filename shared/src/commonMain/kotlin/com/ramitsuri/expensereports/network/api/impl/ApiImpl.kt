package com.ramitsuri.expensereports.network.api.impl

import com.ramitsuri.expensereports.model.CurrentBalance
import com.ramitsuri.expensereports.model.MonthYear
import com.ramitsuri.expensereports.model.Report
import com.ramitsuri.expensereports.model.Transaction
import com.ramitsuri.expensereports.model.toList
import com.ramitsuri.expensereports.network.NotFoundException
import com.ramitsuri.expensereports.network.api.Api
import com.ramitsuri.expensereports.network.apiRequest
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone

internal class ApiImpl(
    private val httpClient: HttpClient,
    private val ioDispatcher: CoroutineDispatcher,
    private val clock: Clock,
    private val timeZone: TimeZone,
) : Api {
    override suspend fun getTransactions(
        baseUrl: String,
        since: MonthYear,
    ): Result<List<Transaction>> =
        coroutineScope {
            getMonthYears(since)
                .map { monthYear ->
                    async {
                        getTransactionsForMonthYear(baseUrl, monthYear)
                    }
                }
                .awaitAll()
                .let { result ->
                    if (result.all { it.isSuccess }) {
                        return@coroutineScope Result.success(
                            result
                                .map { it.getOrThrow() }
                                .flatten(),
                        )
                    }
                    val (successes, errors) =
                        result.partition {
                            it.getOrNull() != null
                        }
                    val errorsOtherThanNotFound =
                        errors.filterNot {
                            it.exceptionOrNull() is NotFoundException
                        }
                    if (errorsOtherThanNotFound.isEmpty()) {
                        return@coroutineScope Result.success(
                            successes
                                .map { it.getOrThrow() }
                                .flatten(),
                        )
                    }
                    Result.failure(errorsOtherThanNotFound.firstNotNullOf { it.exceptionOrNull() })
                }
        }

    override suspend fun getCurrentBalances(
        baseUrl: String,
        since: MonthYear,
    ): Result<List<CurrentBalance>> {
        return apiRequest(ioDispatcher) {
            httpClient.get("$baseUrl/current_balances/current_balances.json")
        }
    }

    override suspend fun getReports(
        baseUrl: String,
        since: MonthYear,
    ): Result<List<Report>> =
        coroutineScope {
            getMonthYears(since)
                .asSequence()
                .map { it.year }
                .distinct()
                .map { year -> getReportsRoutes(year) }
                .flatten()
                .map { route ->
                    async {
                        apiRequest<Report>(ioDispatcher) {
                            httpClient.get("$baseUrl/reports/$route")
                        }
                    }
                }
                .toList()
                .awaitAll()
                .let { result ->
                    if (result.all { it.isSuccess }) {
                        return@coroutineScope Result.success(result.map { it.getOrThrow() })
                    }
                    val (successes, errors) =
                        result.partition {
                            it.getOrNull() != null
                        }
                    val errorsOtherThanNotFound =
                        errors.filterNot {
                            it.exceptionOrNull() is NotFoundException
                        }
                    if (errorsOtherThanNotFound.isEmpty()) {
                        return@coroutineScope Result.success(successes.map { it.getOrThrow() })
                    }
                    Result.failure(errorsOtherThanNotFound.firstNotNullOf { it.exceptionOrNull() })
                }
        }

    private suspend fun getTransactionsForMonthYear(
        baseUrl: String,
        monthYear: MonthYear,
    ) = apiRequest<List<Transaction>>(ioDispatcher) {
        httpClient.get("$baseUrl/transactions/${monthYear.asTxRoute()}")
    }

    // Fetch for next month included because there might be some data
    private fun getMonthYears(since: MonthYear) = (since..(MonthYear.now(clock, timeZone).next())).toList()

    private fun MonthYear.asTxRoute() = string().split("-").joinToString(separator = "/", postfix = ".json")

    private fun getReportsRoutes(forYear: Int) =
        listOf(
            "After_Deduction_Expenses.json",
            "Assets.json",
            "Expenses.json",
            "Income.json",
            "Income_Without_Gains.json",
            "Liabilities.json",
            "NetWorth.json",
            "Savings.json",
            "Savings_Rate.json",
        ).map { "$forYear/$it" }
}
