package com.ramitsuri.expensereports.network

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.serializer

object InstantSerializer : KSerializer<Instant> {
    override val descriptor = PrimitiveSerialDescriptor("Instant", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Instant {
        return Instant.parse(decoder.decodeString())
    }

    override fun serialize(encoder: Encoder, value: Instant) {
        encoder.encodeString(value.toString())
    }
}

object LocalDateSerializer : KSerializer<LocalDate> {
    override val descriptor = PrimitiveSerialDescriptor("LocalDate", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): LocalDate {
        return LocalDate.parse(decoder.decodeString())
    }

    override fun serialize(encoder: Encoder, value: LocalDate) {
        encoder.encodeString(value.toString())
    }
}

object BigDecimalSerializer : KSerializer<BigDecimal> {
    override val descriptor = PrimitiveSerialDescriptor("BigDecimal", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): BigDecimal {
        return BigDecimal.parseString(decoder.decodeString())
    }

    override fun serialize(encoder: Encoder, value: BigDecimal) {
        encoder.encodeString(value.toString())
    }
}

object IntBigDecimalMapSerializer : KSerializer<Map<Int, BigDecimal>> {
    private val serializer: KSerializer<Map<Int, BigDecimal>> =
        MapSerializer(serializer(), BigDecimalSerializer)

    @OptIn(ExperimentalSerializationApi::class)
    override val descriptor: SerialDescriptor
        get() = SerialDescriptor(serialName = "IntBigDecimalMap", original = serializer.descriptor)

    override fun deserialize(decoder: Decoder): Map<Int, BigDecimal> {
        return serializer.deserialize(decoder)
    }

    override fun serialize(encoder: Encoder, value: Map<Int, BigDecimal>) {
        serializer.serialize(encoder, value)
    }
}