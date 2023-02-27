package com.ramitsuri.expensereports.android.utils

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

class CurrencyFormatterKtTest {

    private val locale = Locale.US

    @Test
    fun testFormat() {
        var value = BigDecimal.parseString("987.45")
        var formatted = value.format(stripZeros = true, locale)
        assertEquals("$987.45", formatted)

        value = BigDecimal.parseString("0")
        formatted = value.format(stripZeros = true, locale)
        assertEquals("$0", formatted)

        value = BigDecimal.parseString("10")
        formatted = value.format(stripZeros = true, locale)
        assertEquals("$10", formatted)

        value = BigDecimal.parseString("100.00")
        formatted = value.format(stripZeros = true, locale)
        assertEquals("$100", formatted)

        value = BigDecimal.parseString("100.00")
        formatted = value.format(stripZeros = false, locale)
        assertEquals("$100.00", formatted)
    }

    @Test
    fun testFormatRounded() {
        var value = BigDecimal.parseString("987.45")
        var formatted = value.formatRounded(stripZeros = true, locale)
        assertEquals("$987.45", formatted)

        value = BigDecimal.parseString("1987.45")
        formatted = value.formatRounded(stripZeros = true, locale)
        assertEquals("$1.99K", formatted)

        value = BigDecimal.parseString("11987.45")
        formatted = value.formatRounded(stripZeros = true, locale)
        assertEquals("$11.99K", formatted)

        value = BigDecimal.parseString("111987.45")
        formatted = value.formatRounded(stripZeros = true, locale)
        assertEquals("$111.99K", formatted)

        value = BigDecimal.parseString("1111987.45")
        formatted = value.formatRounded(stripZeros = true, locale)
        assertEquals("$1.11M", formatted)

        value = BigDecimal.parseString("11111987.45")
        formatted = value.formatRounded(stripZeros = true, locale)
        assertEquals("$11.11M", formatted)
    }
}