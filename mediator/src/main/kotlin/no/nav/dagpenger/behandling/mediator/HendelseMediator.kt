package no.nav.dagpenger.behandling.mediator

import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.api.trace.Span
import io.opentelemetry.instrumentation.annotations.WithSpan
import mu.KotlinLogging
import no.nav.dagpenger.aktivitetslogg.aktivitet.Hendelse
import no.nav.dagpenger.behandling.modell.hendelser.PersonHendelse

class HendelseMediator(
    private val rapidsConnection: RapidsConnection,
) {
    private companion object {
        val logger = KotlinLogging.logger { }
        val sikkerlogg = KotlinLogging.logger("tjenestekall.HendelseMediator")
    }

    @WithSpan
    internal fun håndter(hendelse: PersonHendelse) {
        hendelse.kontekster().forEach {
            if (!it.harFunksjonelleFeilEllerVerre()) håndter(hendelse, it.hendelse())
        }
    }

    private fun håndter(
        personhendelse: PersonHendelse,
        hendelser: List<Hendelse>,
    ) {
        val hendelsestyper = hendelser.groupBy { it.type }.mapValues { (_, hendelseliste) -> hendelseliste.single() }

        hendelsestyper.forEach { (type, hendelse) ->
            val data = hendelse.detaljer() + hendelse.kontekst()
            val melding = JsonMessage.newMessage(type.name, data)

            sikkerlogg.info { "sender hendelse ${type.name}:\n${melding.toJson()}}" }
            logger.info { "sender hendelse for ${type.name}" }
            Span.current().addEvent("Publiserer hendelse", Attributes.of(AttributeKey.stringKey("hendelse"), type.name))
            rapidsConnection.publish(personhendelse.ident(), melding.toJson())
        }
    }
}
