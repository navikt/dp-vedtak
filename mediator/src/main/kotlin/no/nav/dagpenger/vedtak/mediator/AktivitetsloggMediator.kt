package no.nav.dagpenger.vedtak.mediator

import no.nav.dagpenger.aktivitetslogg.AktivitetsloggEventMapper
import no.nav.dagpenger.vedtak.modell.hendelser.Hendelse
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.RapidsConnection

internal class AktivitetsloggMediator(private val rapidsConnection: RapidsConnection) {
    private val aktivitetsloggEventMapper = AktivitetsloggEventMapper()

    fun håndter(hendelse: Hendelse) {
        aktivitetsloggEventMapper.håndter(hendelse) { aktivitetLoggMelding ->
            rapidsConnection.publish(
                JsonMessage.newMessage(
                    aktivitetLoggMelding.eventNavn,
                    aktivitetLoggMelding.innhold,
                ).toJson(),
            )
        }
    }
}
