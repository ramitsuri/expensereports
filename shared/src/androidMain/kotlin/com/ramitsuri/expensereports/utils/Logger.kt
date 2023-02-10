package com.ramitsuri.expensereports.utils

import co.touchlab.kermit.Logger

actual class Logger(
    private val enableLocal: Boolean,
    private val deviceDetails: String
) {
    actual fun enableRemoteLogging(enable: Boolean) {
        localLog(tag = TAG, level = Level.VERBOSE, message = "EnableRemote -> $enable")
    }

    actual fun d(tag: String, message: String) {
        localLog(tag, Level.DEBUG, message)
    }

    actual fun v(tag: String, message: String) {
        localLog(tag, Level.VERBOSE, message)
    }

    actual fun e(tag: String, message: String) {
        localLog(tag, Level.ERROR, message)
    }

    private fun localLog(tag: String, level: Level, message: String) {
        if (enableLocal) {
            val logger = Logger.withTag(tag)
            when (level) {
                Level.VERBOSE -> {
                    logger.v(message)
                }
                Level.DEBUG -> {
                    logger.d(message)
                }
                Level.ERROR -> {
                    logger.e(message)
                }
            }
        }
    }

    private enum class Level {
        VERBOSE,
        DEBUG,
        ERROR
    }

    companion object {
        private const val TAG = "Logger"
    }
}