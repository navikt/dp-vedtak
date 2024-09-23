package no.nav.dagpenger.behandling.mediator

import io.opentelemetry.api.trace.Span
import io.opentelemetry.instrumentation.annotations.WithSpan
import no.nav.dagpenger.aktivitetslogg.AktivitetsloggEventMapper
import no.nav.dagpenger.aktivitetslogg.AktivitetsloggHendelse
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.RapidsConnection

internal class AktivitetsloggMediator(private val rapidsConnection: RapidsConnection) {
    private val aktivitetsloggEventMapper = AktivitetsloggEventMapper()

    @WithSpan
    fun håndter(hendelse: AktivitetsloggHendelse) {
        Span.current().addEvent("Publiserer aktivitetslogg")
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
