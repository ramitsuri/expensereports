package com.ramitsuri.expensereports.model

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RunInfo(
    @SerialName("file_last_modified_time")
    val fileLastModifiedTime: Instant,
    @SerialName("last_run_time")
    val lastRunTime: Instant,
)
