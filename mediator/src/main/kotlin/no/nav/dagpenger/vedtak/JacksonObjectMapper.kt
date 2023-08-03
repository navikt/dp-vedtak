package no.nav.dagpenger.vedtak

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.blackbird.BlackbirdModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.dagpenger.vedtak.mediator.persistens.AktivitetsloggDTO
import java.io.InputStream

val objectMapper = jacksonObjectMapper().apply {
    disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
    setSerializationInclusion(JsonInclude.Include.NON_NULL)
    configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    registerModule(BlackbirdModule())
    registerModule(JavaTimeModule())
}

fun InputStream.aktivitetslogg(): AktivitetsloggDTO = this.use { objectMapper.readValue(it, AktivitetsloggDTO::class.java) }
