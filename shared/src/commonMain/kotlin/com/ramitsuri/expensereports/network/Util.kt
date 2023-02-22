package com.ramitsuri.expensereports.network

import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import io.ktor.utils.io.errors.IOException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException

suspend inline fun <reified T> apiRequest(
    ioDispatcher: CoroutineDispatcher,
    crossinline call: suspend () -> HttpResponse
): NetworkResponse<T> {
    return withContext(ioDispatcher) {
        var exception: Throwable? = null
        val response: HttpResponse? = try {
            call()
        } catch (e: Exception) {
            exception = e
            null
        }
        return@withContext when {
            response?.status == HttpStatusCode.OK -> {
                val data: T = response.body()
                NetworkResponse.Success(data)
            }
            exception is ClientRequestException -> {
                NetworkResponse.Failure(NetworkError.INVALID_REQUEST, exception)
            }
            exception is ServerResponseException -> {
                NetworkResponse.Failure(NetworkError.SERVER, exception)
            }
            exception is IOException -> {
                NetworkResponse.Failure(NetworkError.NETWORK, exception)
            }
            exception is SerializationException -> {
                NetworkResponse.Failure(NetworkError.SERIALIZATION, exception)
            }
            else -> {
                NetworkResponse.Failure(NetworkError.UNKNOWN, exception)
            }
        }
    }

}