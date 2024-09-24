package no.nav.dagpenger.behandling.mediator

import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageContext
import mu.KotlinLogging
import no.nav.dagpenger.behandling.modell.BehandlingObservatør.BehandlingEndretTilstand
import no.nav.dagpenger.behandling.modell.PersonObservatør
import no.nav.dagpenger.behandling.modell.hendelser.PersonHendelse

internal class PersonMediator(
    private val hendelse: PersonHendelse,
) : PersonObservatør {
    private val meldinger = mutableListOf<JsonMessage>()

    private companion object {
        val logger = KotlinLogging.logger { }
        val sikkerlogg = KotlinLogging.logger("tjenestekall")
    }

    override fun endretTilstand(event: BehandlingEndretTilstand) {
        meldinger.add(event.toJsonMessage())
    }

    internal fun ferdigstill(context: MessageContext) {
        meldinger.forEach { context.publish(it.toJson()) }
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
}
