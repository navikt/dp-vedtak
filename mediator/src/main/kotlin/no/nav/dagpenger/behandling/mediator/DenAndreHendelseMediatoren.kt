package no.nav.dagpenger.behandling.mediator

import mu.KotlinLogging
import no.nav.dagpenger.aktivitetslogg.aktivitet.Hendelse
import no.nav.dagpenger.behandling.modell.hendelser.PersonHendelse
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.RapidsConnection

class DenAndreHendelseMediatoren(private val rapidsConnection: RapidsConnection) {
    private companion object {
        val logger = KotlinLogging.logger { }
        val sikkerlogg = KotlinLogging.logger("tjenestekall.DenAndreHendelseMediatoren")
    }

    internal fun håndter(hendelse: PersonHendelse) {
        hendelse.kontekster().forEach {
            if (!it.harFunksjonelleFeilEllerVerre()) håndter(hendelse, it.hendelse())
        }
    }

    private fun håndter(
        hendelse: PersonHendelse,
        hendelser: List<Hendelse>,
    ) {
        val hendelsestyper = hendelser.groupBy { it.type }.mapValues { (_, behovList) -> behovList.single() }

        hendelsestyper.forEach { (type, behovMap) ->
            val data = behovMap.detaljer() + behovMap.kontekst()
            val melding = JsonMessage.newMessage(type.name, data)
            sikkerlogg.info { "sender hendelse ${type.name}:\n${melding.toJson()}}" }
            logger.info { "sender hendelse for ${type.name}" }
            rapidsConnection.publish(hendelse.ident(), melding.toJson())
        }
    }
}
