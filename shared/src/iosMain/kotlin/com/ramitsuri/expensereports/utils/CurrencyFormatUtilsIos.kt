package com.ramitsuri.expensereports.utils

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import platform.Foundation.NSLocale
import platform.Foundation.NSNumberFormatter
import platform.Foundation.NSNumberFormatterCurrencyStyle

actual fun BigDecimal.format(stripZeros: Boolean, localeIdentifier: String): String {
    val formatter = NSNumberFormatter()
    val number = formatter.numberFromString(this.toStringExpanded()) ?: return ""
    formatter.numberStyle = NSNumberFormatterCurrencyStyle
    formatter.locale = NSLocale(localeIdentifier = localeIdentifier)
    val value = formatter.stringFromNumber(number) ?: ""
    return if (stripZeros && value.contains(".0")) {
        value.replace("0*$".toRegex(), "")
            .replace("\\.$".toRegex(), "")
    } else {
        value
    }
}
