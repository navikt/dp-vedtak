package no.nav.dagpenger.vedtak.iverksetting.mediator.behovløsere

import kotlinx.coroutines.runBlocking
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
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import no.nav.helse.rapids_rivers.asLocalDate
import no.nav.helse.rapids_rivers.asLocalDateTime
import java.util.UUID

internal class IverksettBehovløser(
    rapidsConnection: RapidsConnection,
    private val iverksettClient: IverksettClient,
) : River.PacketListener {

    init {
        River(rapidsConnection).apply {
            validate { it.requireValue("@event_name", "behov") }
            validate { it.demandAllOrAny("@behov", listOf("Iverksett")) }
            validate { it.requireKey("ident") }
            validate { it.requireKey("Iverksett.vedtakId") }
            validate { it.requireKey("Iverksett.behandlingId") }
            validate { it.requireKey("Iverksett.vedtakstidspunkt") }
            validate { it.requireKey("Iverksett.virkningsdato") }
            validate { it.requireKey("Iverksett.utfall") }
        }.register(this)
    }
    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        val vedtakId = packet["Iverksett.vedtakId"].asText()
        val iverksettDagpengerDto = packet.tilIverksettDagpengerDTO()
        runBlocking {
            iverksettClient.iverksett(iverksettDagpengerDto)
        }
        packet["@løsning"] = mapOf("Iverksatt" to true)
        context.publish(packet.toJson())
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
        behandlingId = packet["Iverksett.behandlingId"].asText().let { UUID.fromString(it) },
        behandlingType = BehandlingType.FØRSTEGANGSBEHANDLING,
        behandlingÅrsak = BehandlingÅrsak.SØKNAD,
    )

private fun vedtaksdetaljerDagpengerDto(packet: JsonMessage) =
    VedtaksdetaljerDagpengerDto(
        vedtakstidspunkt = packet["Iverksett.vedtakstidspunkt"].asLocalDateTime(),
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
                fraOgMedDato = packet["Iverksett.virkningsdato"].asLocalDate(),
            ),
        ),
    )
private fun JsonMessage.utfall(): String = this["Iverksett.utfall"].asText()
