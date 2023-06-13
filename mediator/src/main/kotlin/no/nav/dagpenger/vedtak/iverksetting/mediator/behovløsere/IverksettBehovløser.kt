package no.nav.dagpenger.vedtak.iverksetting.mediator.behovløsere

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.slf4j.MDCContext
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import mu.withLoggingContext
import no.nav.dagpenger.kontrakter.iverksett.IverksettDto
import no.nav.dagpenger.kontrakter.iverksett.VedtaksdetaljerDto
import no.nav.dagpenger.kontrakter.iverksett.VedtaksperiodeDto
import no.nav.dagpenger.kontrakter.iverksett.Vedtaksresultat
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.MessageProblems
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import no.nav.helse.rapids_rivers.asLocalDate
import no.nav.helse.rapids_rivers.asLocalDateTime
import java.util.UUID

private val BehovIverksett = "Iverksett"

val behandlingId = "behandlingId"

internal class IverksettBehovløser(
    rapidsConnection: RapidsConnection,
    private val iverksettClient: IverksettClient,
) : River.PacketListener {

    private companion object {
        val logger = KotlinLogging.logger { }
    }

    init {
        River(rapidsConnection).apply {
            validate { it.requireValue("@event_name", "behov") }
            validate { it.demandAllOrAny("@behov", listOf(BehovIverksett)) }
            validate { it.requireKey("ident") }
            validate { it.requireKey("$BehovIverksett.vedtakId") }
            validate { it.requireKey("$BehovIverksett.behandlingId") }
            validate { it.requireKey("$BehovIverksett.vedtakstidspunkt") }
            validate { it.requireKey("$BehovIverksett.virkningsdato") }
            validate { it.requireKey("$BehovIverksett.utbetalingsdager") }
            validate { it.requireKey("$BehovIverksett.utfall") }
            validate { it.interestedIn("@behovId", "iverksettingId") }
            validate { it.rejectKey("@løsning") }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        withLoggingContext(
            mapOf(
                behandlingId to packet["$BehovIverksett.behandlingId"].asText(),
                "vedtakId" to packet["$BehovIverksett.vedtakId"].asText(),
                "iverksettingId" to packet["iverksettingId"].asText(),
                "behovId" to packet["@behovId"].asText(),
            ),
        ) {
            logger.info { "Fått behov $BehovIverksett" }

            val iverksettDagpengerDto = packet.tilIverksettDagpengerDTO()
            runBlocking {
                withContext(MDCContext()) {
                    iverksettClient.iverksett(iverksettDagpengerDto)
                }
            }
            packet["@løsning"] = mapOf(BehovIverksett to true)
            context.publish(packet.toJson())
            logger.info { "Løste behov $BehovIverksett" }
        }
    }

    override fun onError(problems: MessageProblems, context: MessageContext) {
        logger.warn {
            "Kunne ikke lese $BehovIverksett: ${problems.toExtendedReport()}"
        }
    }
}

internal fun JsonMessage.tilIverksettDagpengerDTO(): IverksettDto = IverksettDto(
    sakId = UUID.randomUUID(),
    behandlingId = this["$BehovIverksett.behandlingId"].asText().let { UUID.fromString(it) },
    personIdent = this["ident"].asText(),
    vedtak = vedtaksdetaljerDagpengerDto(this),
)

private fun vedtaksdetaljerDagpengerDto(packet: JsonMessage) =
    VedtaksdetaljerDto(
        vedtakstidspunkt = packet["$BehovIverksett.vedtakstidspunkt"].asLocalDateTime(),
        resultat = when (packet.utfall()) {
            "Innvilget" -> Vedtaksresultat.INNVILGET
            "Avslått" -> Vedtaksresultat.AVSLÅTT
            else -> {
                throw IllegalArgumentException("Ugyldig utfall - vet ikke hvordan en mapper ${packet.utfall()} ")
            }
        },

        saksbehandlerId = "DIGIDAG",
        beslutterId = "DIGIDAG",
        vedtaksperioder = listOf(
            VedtaksperiodeDto(
                fraOgMedDato = packet["$BehovIverksett.virkningsdato"].asLocalDate(),
            ),
        ),
    )

private fun JsonMessage.utfall(): String = this["$BehovIverksett.utfall"].asText()
