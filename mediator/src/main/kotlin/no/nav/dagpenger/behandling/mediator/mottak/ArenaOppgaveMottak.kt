package no.nav.dagpenger.behandling.mediator.mottak

import com.fasterxml.jackson.databind.JsonNode
import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers.River
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageContext
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageMetadata
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import io.micrometer.core.instrument.MeterRegistry
import io.opentelemetry.instrumentation.annotations.WithSpan
import kotliquery.queryOf
import kotliquery.sessionOf
import mu.KotlinLogging
import mu.withLoggingContext
import no.nav.dagpenger.behandling.db.PostgresDataSourceBuilder.dataSource
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

internal class ArenaOppgaveMottak(
    rapidsConnection: RapidsConnection,
    private val sakRepository: SakRepository,
) : River.PacketListener {
    init {
        River(rapidsConnection)
            .apply {
                precondition { it.requireValue("op_type", "U") }
                validate { it.requireKey("pos") }
                validate { it.require("op_ts", JsonNode::asArenaDato) }
                validate {
                    it.requireKey(
                        "after.SAK_ID",
                        "after.DESCRIPTION",
                        "after.OPPGAVETYPE_BESKRIVELSE",
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
        metadata: MessageMetadata,
        meterRegistry: MeterRegistry,
    ) {
        val sakId = packet["after.SAK_ID"].toString()
        withLoggingContext("sakId" to sakId) {
            logger.info { "Mottok oppgave fra Arena, vurderer om noe skal avbrytes" }

            val behandling = sakRepository.finnBehandling(sakId.toInt())
            if (behandling == null) {
                logger.info { "Fant ingen behandling for sakId=$sakId som skal avbrytes" }
                return@withLoggingContext
            }

            val beskrivelse = packet["after.OPPGAVETYPE_BESKRIVELSE"].toString()
            logger.info { "Publiserer avbrytmelding for ${behandling.behandlingId}, mottok oppgave av type=$beskrivelse" }
            sikkerlogg.info { "Mottok oppgave fra Arena. Pakke=${packet.toJson()}" }
        }
    }

    private companion object {
        private val logger = KotlinLogging.logger {}
        private val sikkerlogg = KotlinLogging.logger("tjenestekall.ArenaOppgaveMottak")
    }
}

class SakRepository {
    fun finnBehandling(fagsakId: Int): Beeeeehandling? =
        sessionOf(dataSource).use {
            it.run(
                queryOf(
                    //language=PostgreSQL
                    """
                    SELECT bo.behandling_id, pb.ident
                    FROM opplysningstabell
                             LEFT JOIN behandling_opplysninger bo ON opplysningstabell.opplysninger_id = bo.opplysninger_id
                             LEFT JOIN behandling b ON bo.behandling_id = b.behandling_id
                             LEFT JOIN person_behandling pb ON b.behandling_id = pb.behandling_id
                    WHERE type_id = 'fagsakId'
                      AND verdi_heltall = :fagsakId
                      AND b.tilstand != 'Ferdig'
                    """.trimIndent(),
                    mapOf("fagsakId" to fagsakId),
                ).map { row ->
                    Beeeeehandling(
                        row.string("ident"),
                        row.string("behandling_id"),
                    )
                }.asSingle,
            )
        }

    data class Beeeeehandling(
        val ident: String,
        val behandlingId: String,
    )
}

private var arenaDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss[.SSSSSS]")

private fun JsonNode.asArenaDato(): LocalDateTime = asText().let { LocalDateTime.parse(it, arenaDateFormatter) }
