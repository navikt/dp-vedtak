package no.nav.dagpenger.behandling.mediator.mottak

import com.fasterxml.jackson.databind.JsonNode
import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers.River
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageContext
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import io.opentelemetry.instrumentation.annotations.WithSpan
import mu.KotlinLogging
import mu.withLoggingContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

internal class ArenaOppgaveMottak(
    rapidsConnection: RapidsConnection,
) : River.PacketListener {
    init {
        River(rapidsConnection)
            .apply {
                validate { it.requireKey("op_type", "pos") }
                validate { it.require("op_ts", JsonNode::asArenaDato) }
                validate {
                    it.requireKey(
                        "after.SAK_ID",
                        "after.SAK_TYPE",
                    )
                }
                validate { it.require("after.REG_DATO", JsonNode::asArenaDato) }
                validate { it.require("after.MOD_DATO", JsonNode::asArenaDato) }
            }.register(this)
    }

    @WithSpan
    override fun onPacket(
        packet: JsonMessage,
        context: MessageContext,
    ) {
        val sakId = packet["after.SAK_ID"].toString()
        withLoggingContext("sakId" to sakId) {
            logger.info { "Mottok oppgave fra Arena" }
            sikkerlogg.info { "Mottok oppgave fra Arena. Pakke=${packet.toJson()}" }
        }
    }

    private companion object {
        private val logger = KotlinLogging.logger {}
        private val sikkerlogg = KotlinLogging.logger("tjenestekall.ArenaOppgaveMottak")
    }
}

private var arenaDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss[.SSSSSS]")

private fun JsonNode.asArenaDato(): LocalDateTime = asText().let { LocalDateTime.parse(it, arenaDateFormatter) }
