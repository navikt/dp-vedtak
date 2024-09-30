package no.nav.dagpenger.behandling.mediator

import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageContext
import mu.KotlinLogging
import no.nav.dagpenger.behandling.api.models.VedtakDTO
import no.nav.dagpenger.behandling.modell.BehandlingObservatør.BehandlingEndretTilstand
import no.nav.dagpenger.behandling.modell.BehandlingObservatør.BehandlingFerdig
import no.nav.dagpenger.behandling.modell.Ident
import no.nav.dagpenger.behandling.modell.PersonObservatør
import no.nav.dagpenger.behandling.modell.hendelser.PersonHendelse

typealias Hendelse = Pair<String, JsonMessage>

internal class PersonMediator(
    private val hendelse: PersonHendelse,
) : PersonObservatør {
    private val meldinger = mutableListOf<Hendelse>()

    private companion object {
        val logger = KotlinLogging.logger { }
        val sikkerlogg = KotlinLogging.logger("tjenestekall")
    }

    override fun endretTilstand(event: BehandlingEndretTilstand) {
        val ident = requireNotNull(event.ident) { "Mangler ident i BehandlingEndretTilstand" }
        meldinger.add(ident to event.toJsonMessage())
    }

    override fun ferdig(event: BehandlingFerdig) {
        val ident = requireNotNull(event.ident) { "Mangler ident i BehandlingFerdig" }
        meldinger.add(ident to event.toJsonMessage())
    }

    internal fun ferdigstill(context: MessageContext) {
        meldinger.forEach { context.publish(it.first, it.second.toJson()) }
    }

    private fun BehandlingEndretTilstand.toJsonMessage() =
        JsonMessage
            .newMessage(
                "behandling_endret_tilstand",
                mapOf(
                    "ident" to requireNotNull(ident) { "Mangler ident i BehandlingEndretTilstand" },
                    "behandlingId" to behandlingId.toString(),
                    "forrigeTilstand" to forrigeTilstand.name,
                    "gjeldendeTilstand" to gjeldendeTilstand.name,
                    "forventetFerdig" to forventetFerdig.toString(),
                    "tidBrukt" to tidBrukt.toString(),
                ),
            )

    private fun BehandlingFerdig.toJsonMessage(): JsonMessage {
        val ident = Ident(requireNotNull(ident) { "Mangler ident i BehandlingEndretTilstand" })
        val vedtak = lagVedtak(behandlingId, ident, søknadId, opplysninger, automatiskBehandlet)

        return JsonMessage.newMessage("vedtak_fattet", vedtak.toMap())
    }

    private fun VedtakDTO.toMap() =
        listOfNotNull(
            "behandlingId" to behandlingId,
            "fagsakId" to fagsakId,
            "søknadId" to søknadId,
            "vedtakstidspunkt" to vedtakstidspunkt,
            "virkningsdato" to virkningsdato,
            "fastsatt" to fastsatt,
            "ident" to ident,
            automatisk?.let { "automatisk" to it },
            gjenstående?.let { "gjenstående" to it },
            "behandletAv" to behandletAv,
            "vilkår" to vilkår,
            "utbetalinger" to utbetalinger,
            "opplysninger" to opplysninger,
        ).toMap()
}
