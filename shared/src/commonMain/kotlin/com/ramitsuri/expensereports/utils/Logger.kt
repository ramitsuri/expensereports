package com.ramitsuri.expensereports.utils

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

expect class Logger {
    fun enableRemoteLogging(enable: Boolean)
    fun d(tag: String, message: String)
    fun v(tag: String, message: String)
    fun e(tag: String, message: String)
}

object LogHelper : KoinComponent {
    private val logger: Logger by inject()

    fun d(tag: String, message: String) {
        logger.d(tag, message)
    }

    fun e(tag: String, message: String) {
        logger.e(tag, message)
    }

    fun v(tag: String, message: String) {
        logger.v(tag, message)
    }
}