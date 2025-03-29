package com.ramitsuri.expensereports.network

import com.ramitsuri.expensereports.log.logE
import com.ramitsuri.expensereports.log.logI
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

internal suspend inline fun <reified T> apiRequest(
    ioDispatcher: CoroutineDispatcher,
    crossinline call: suspend () -> HttpResponse,
): Result<T> {
    return withContext(ioDispatcher) {
        try {
            val response: HttpResponse = call()
            when {
                response.status == HttpStatusCode.OK -> {
                    val data: T = response.body()
                    logI(TAG) { "Success and have data: $data" }
                    Result.success(data)
                }

                response.status == HttpStatusCode.Created && T::class == Unit::class -> {
                    logI(TAG) { "Success without data" }
                    Result.success(response.body())
                }

                response.status == HttpStatusCode.BadRequest -> {
                    logE(TAG) { "Bad request" }
                    Result.failure(BadRequestException)
                }

                response.status == HttpStatusCode.NotFound -> {
                    logE(TAG) { "Not found" }
                    Result.failure(NotFoundException)
                }

                else -> {
                    logE(TAG) { "Failed: ${response.status}" }
                    Result.failure(UnknownErrorException())
                }
            }
        } catch (e: Exception) {
            if (e is java.io.IOException) {
                logE(TAG, e) { "Failed: probably connectivity issues" }
                Result.failure(NoInternetException)
            } else {
                logE(TAG, e) { "Failed: exception" }
                Result.failure(UnknownErrorException(e))
            }
        }
    }
}

private const val TAG = "ApiRequest"

data object BadRequestException : RuntimeException("Bad request") {
    private fun readResolve(): Any = BadRequestException
}

data object NoInternetException : RuntimeException("No internet") {
    private fun readResolve(): Any = NoInternetException
}

data object NotFoundException : RuntimeException("Not found") {
    private fun readResolve(): Any = NotFoundException
}

data class UnknownErrorException(val exception: Exception? = null) :
    RuntimeException("Unknown error")
