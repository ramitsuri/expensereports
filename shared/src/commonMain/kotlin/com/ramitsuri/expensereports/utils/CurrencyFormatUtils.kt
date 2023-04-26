package com.ramitsuri.expensereports.utils

import com.ionspin.kotlin.bignum.decimal.BigDecimal

fun BigDecimal.format() = format(stripZeros = true, localeIdentifier = "en-US")

expect fun BigDecimal.format(stripZeros: Boolean = true, localeIdentifier: String = "en-US"): String

fun BigDecimal.formatRounded(stripZeros: Boolean = true, localeIdentifier: String = "en-US"): String {
    val value = format(stripZeros, localeIdentifier)
    return if (this < thousand) {
        return value
    } else if (this >= thousand && this < million) {
        "${this.divide(thousand).format(stripZeros, localeIdentifier)}K"
    } else {
        "${this.divide(million).format(stripZeros, localeIdentifier)}M"
    }
}

internal val thousand = BigDecimal.parseString("1000")
internal val million = thousand.multiply(thousand)