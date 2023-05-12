package no.nav.dagpenger.vedtak.iverksetting.mediator.behovløsere

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.slf4j.MDCContext
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import mu.withLoggingContext
import no.nav.dagpenger.vedtak.iverksetting.mediator.behovløsere.models.BehandlingType
import no.nav.dagpenger.vedtak.iverksetting.mediator.behovløsere.models.BehandlingsdetaljerDto
import no.nav.dagpenger.vedtak.iverksetting.mediator.behovløsere.models.BehandlingÅrsak
import no.nav.dagpenger.vedtak.iverksetting.mediator.behovløsere.models.IverksettDagpengerdDto
import no.nav.dagpenger.vedtak.iverksetting.mediator.behovløsere.models.SakDto
import no.nav.dagpenger.vedtak.iverksetting.mediator.behovløsere.models.SøkerDto
import no.nav.dagpenger.vedtak.iverksetting.mediator.behovløsere.models.VedtaksdetaljerDagpengerDto
import no.nav.dagpenger.vedtak.iverksetting.mediator.behovløsere.models.VedtaksperiodeDagpengerDto
import no.nav.dagpenger.vedtak.iverksetting.mediator.behovløsere.models.Vedtaksresultat
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
            println(packet.toJson())
            logger.info { "Løste behov $BehovIverksett" }
        }
    }

    override fun onError(problems: MessageProblems, context: MessageContext) {
        logger.warn {
            "Kunne ikke lese $BehovIverksett: ${problems.toExtendedReport()}"
        }
    }
}

internal fun JsonMessage.tilIverksettDagpengerDTO(): IverksettDagpengerdDto = IverksettDagpengerdDto(
    sak = SakDto(
        sakId = UUID.randomUUID(),
    ),
    behandling = behandlingsdetaljerDto(this),
    søker = SøkerDto(
        personIdent = this["ident"].asText(),
    ),
    vedtak = vedtaksdetaljerDagpengerDto(this),
)
private fun behandlingsdetaljerDto(packet: JsonMessage) =
    BehandlingsdetaljerDto(
        behandlingId = packet["$BehovIverksett.behandlingId"].asText().let { UUID.fromString(it) },
        behandlingType = BehandlingType.FØRSTEGANGSBEHANDLING,
        behandlingÅrsak = BehandlingÅrsak.SØKNAD,
    )

private fun vedtaksdetaljerDagpengerDto(packet: JsonMessage) =
    VedtaksdetaljerDagpengerDto(
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
            VedtaksperiodeDagpengerDto(
                fraOgMedDato = packet["$BehovIverksett.virkningsdato"].asLocalDate(),
            ),
        ),
    )
private fun JsonMessage.utfall(): String = this["$BehovIverksett.utfall"].asText()
