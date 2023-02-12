package com.ramitsuri.expensereports.network

import co.touchlab.kermit.StaticConfig
import co.touchlab.kermit.platformLogWriter
import com.ramitsuri.expensereports.data.prefs.PrefManager
import com.ramitsuri.expensereports.utils.DispatcherProvider
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import co.touchlab.kermit.Logger as KermitLogger
import io.ktor.client.plugins.logging.Logger as KtorLogger

class NetworkProvider(
    private val dispatcherProvider: DispatcherProvider,
    private val prefManager: PrefManager,
    clientEngine: HttpClientEngine
) {
    private val log: KermitLogger = KermitLogger(
        StaticConfig(logWriterList = listOf(platformLogWriter())),
        "Network"
    )

    private val client = HttpClient(clientEngine) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }

        install(HttpTimeout) {
            val timeout = API_TIME_OUT
            connectTimeoutMillis = timeout
            requestTimeoutMillis = timeout
            socketTimeoutMillis = timeout
        }

        install(Logging) {
            logger = object : KtorLogger {
                override fun log(message: String) {
                    log.v { message }
                }
            }

            level = LogLevel.INFO
        }

        install(DefaultRequest) {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
        }
    }

    fun reportApi(): ReportApi {
        return if (prefManager.getServerUrl().isEmpty()) {
            DummyReportApiImpl()
        } else {
            ReportApiImpl(client, prefManager.getServerUrl(), dispatcherProvider)
        }
    }

    companion object {
        const val API_TIME_OUT: Long = 30_000
    }
}