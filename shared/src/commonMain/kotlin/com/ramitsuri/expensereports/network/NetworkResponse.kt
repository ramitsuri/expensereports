package com.ramitsuri.expensereports.network

sealed class NetworkResponse<out T> {
    data class Success<T>(val data: T) : NetworkResponse<T>()

    data class Failure(
        val error: NetworkError,
        val throwable: Throwable?
    ) : NetworkResponse<Nothing>()
}

inline fun <reified T> NetworkResponse<T>.onFailure(
    callback: (error: NetworkError, throwable: Throwable?) -> Unit
) {
    if (this is NetworkResponse.Failure) {
        callback(error, throwable)
    }
}

inline fun <reified T> NetworkResponse<T>.onSuccess(
    callback: (value: T) -> Unit
) {
    if (this is NetworkResponse.Success) {
        callback(data)
    }
}

enum class NetworkError {
    INVALID_REQUEST,
    SERVER,
    NETWORK,
    SERIALIZATION,
    UNKNOWN
}