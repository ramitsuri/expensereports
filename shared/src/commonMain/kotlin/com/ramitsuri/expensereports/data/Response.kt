package com.ramitsuri.expensereports.data

sealed class Response<out T> {
    data class Success<T>(val data: T) : Response<T>()

    data class Failure(
        val error: Error
    ) : Response<Nothing>()
}

inline fun <reified T> Response<T>.onFailure(
    callback: (error: Error) -> Unit
) {
    if (this is Response.Failure) {
        callback(error)
    }
}

inline fun <reified T> Response<T>.onSuccess(
    callback: (value: T) -> Unit
) {
    if (this is Response.Success) {
        callback(data)
    }
}

enum class Error {
    UNAVAILABLE,
    UNKNOWN
}