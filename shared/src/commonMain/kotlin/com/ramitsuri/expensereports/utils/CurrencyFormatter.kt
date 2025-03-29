package com.ramitsuri.expensereports.utils

import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale
import java.math.BigDecimal

fun BigDecimal.format(stripZeros: Boolean = true, locale: Locale = Locale.US): String {
    val value = currencyFormatter(locale).format(roundForCalculation(this, locale))
    return if (stripZeros && value.contains(".0")) {
        value.replace("0*$".toRegex(), "")
            .replace("\\.$".toRegex(), "")
    } else {
        value
    }
}

fun BigDecimal.formatRounded(stripZeros: Boolean = true, locale: Locale = Locale.US): String {
    val value = format(stripZeros, locale)
    return if (this < thousand) {
        return value
    } else if (this >= thousand && this < million) {
        "${this.divide(thousand).format(stripZeros, locale)}K"
    } else {
        "${this.divide(million).format(stripZeros, locale)}M"
    }
}

fun BigDecimal.formatPercent(locale: Locale = Locale.US) =
    "${roundForCalculation((this * BigDecimal(100)), locale)}"
        .let {
            if (it.contains(".0")) {
                it.replace(".0*$".toRegex(), "")
            } else {
                it
            }
        }
        .plus("%")

fun BigDecimal.div(divisor: BigDecimal): BigDecimal = this.divide(
    divisor,
    10,
    RoundingMode.HALF_EVEN
)

private fun roundForCalculation(amount: BigDecimal, locale: Locale): BigDecimal {
    val jvmBigDecimal = amount.toJvm()
    val newScale = currencyFormatter(locale).maximumFractionDigits
    return jvmBigDecimal.setScale(newScale, RoundingMode.HALF_EVEN)
}

private fun BigDecimal.toJvm() = BigDecimal(this.toPlainString())

private fun currencyFormatter(locale: Locale): DecimalFormat {
    return NumberFormat.getCurrencyInstance(locale) as DecimalFormat
}

private val thousand = BigDecimal("1000")
private val million = thousand.multiply(thousand)