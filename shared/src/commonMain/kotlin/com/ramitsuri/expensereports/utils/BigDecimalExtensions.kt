package com.ramitsuri.expensereports.utils

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.DecimalMode

fun BigDecimal.inverse(): BigDecimal {
    val negativeOne = BigDecimal.parseString("-1")
    return this.multiply(negativeOne)
}

fun String.bd(): BigDecimal {
    return try {
        BigDecimal.parseString(this)
    } catch (e: Exception) {
        BigDecimal.ZERO
    }
}

fun BigDecimal.by(other: String): BigDecimal {
    return try {
        this.divide(other.bd(), DecimalMode.US_CURRENCY)
    } catch (e: Exception) {
        BigDecimal.ZERO
    }
}