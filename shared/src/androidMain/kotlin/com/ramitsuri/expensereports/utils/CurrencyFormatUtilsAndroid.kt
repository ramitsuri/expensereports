package com.ramitsuri.expensereports.utils

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale

actual fun BigDecimal.format(stripZeros: Boolean, localeIdentifier: String): String {
    val locale = Locale.forLanguageTag(localeIdentifier)
    val value = currencyFormatter(locale).format(roundForCalculation(this, locale))
    return if (stripZeros && value.contains(".0")) {
        value.replace("0*$".toRegex(), "")
            .replace("\\.$".toRegex(), "")
    } else {
        value
    }
}

private fun roundForCalculation(amount: BigDecimal, locale: Locale): java.math.BigDecimal {
    val jvmBigDecimal = amount.toJvm()
    val newScale = currencyFormatter(locale).maximumFractionDigits
    return jvmBigDecimal.setScale(newScale, RoundingMode.HALF_EVEN)
}

private fun BigDecimal.toJvm() = java.math.BigDecimal(this.toPlainString())

private fun currencyFormatter(locale: Locale): DecimalFormat {
    return NumberFormat.getCurrencyInstance(locale) as DecimalFormat
}
