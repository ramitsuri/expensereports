package com.ramitsuri.expensereports.android.utils

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale
import java.math.BigDecimal as JvmBigDecimal

fun BigDecimal.format(stripZeros: Boolean = true, locale: Locale = Locale.US): String {
    val value = currencyFormatter(locale).format(roundForCalculation(this, locale))
    return if (stripZeros && value.contains(".0")) {
        value.replace("0*$".toRegex(), "")
            .replace("\\.$".toRegex(), "")
    } else {
        value
    }
}

private fun roundForCalculation(amount: BigDecimal, locale: Locale): JvmBigDecimal {
    val jvmBigDecimal = amount.toJvm()
    val newScale = currencyFormatter(locale).maximumFractionDigits
    return jvmBigDecimal.setScale(newScale, RoundingMode.HALF_EVEN)
}

private fun BigDecimal.toJvm() = JvmBigDecimal(this.toPlainString())

private fun currencyFormatter(locale: Locale): DecimalFormat {
    return NumberFormat.getCurrencyInstance(locale) as DecimalFormat
}