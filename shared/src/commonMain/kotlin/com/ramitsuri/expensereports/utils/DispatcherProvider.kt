package com.ramitsuri.expensereports.utils

import kotlinx.coroutines.CoroutineDispatcher

expect class DispatcherProvider {
    val io: CoroutineDispatcher
    val default: CoroutineDispatcher
    val main: CoroutineDispatcher
}