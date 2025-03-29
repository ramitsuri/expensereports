package com.ramitsuri.expensereports.network

import com.ramitsuri.expensereports.model.MonthYear
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.math.BigDecimal

class BigDecimalSerializer : KSerializer<BigDecimal> {
    override val descriptor = PrimitiveSerialDescriptor("BigDecimal", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): BigDecimal {
        return BigDecimal(decoder.decodeString())
    }

    override fun serialize(encoder: Encoder, value: BigDecimal) {
        encoder.encodeString(value.toString())
    }
}

class MonthYearSerializer : KSerializer<MonthYear> {
    override val descriptor = PrimitiveSerialDescriptor("MonthYear", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: MonthYear) {
        encoder.encodeString(value.string())
    }

    override fun deserialize(decoder: Decoder): MonthYear {
        return decoder.decodeString().let { string ->
            MonthYear.fromString(string)
        }
    }
}
