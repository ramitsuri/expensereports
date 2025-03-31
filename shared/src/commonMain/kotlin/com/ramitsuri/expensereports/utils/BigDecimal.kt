package com.ramitsuri.expensereports.utils

import com.ramitsuri.expensereports.log.logE
import java.math.BigDecimal

fun <T> Map<T, BigDecimal>.getOrThrow(t: T): BigDecimal {
    return this[t] ?: kotlin.run {
        logE("BigDecimal getOrThrow") { "Map does not contain $t" }
        error("BigDecimal getOrThrow Map does not contain $t")
    }
}
