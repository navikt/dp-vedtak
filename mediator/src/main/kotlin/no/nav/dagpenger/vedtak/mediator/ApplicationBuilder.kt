package no.nav.dagpenger.vedtak.mediator

import mu.KotlinLogging
import no.nav.dagpenger.vedtak.mediator.api.vedtakApi
import no.nav.dagpenger.vedtak.mediator.melding.InMemoryMeldingRepository
import no.nav.dagpenger.vedtak.modell.Person
import no.nav.dagpenger.vedtak.modell.PersonIdentifikator
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import kotlin.math.log

internal class ApplicationBuilder(config: Map<String, String>) : RapidsConnection.StatusListener {
    companion object {
        private val logger = KotlinLogging.logger { }
    }

    private val personRepository =
        object : PersonRepository {
            private val personer = mutableMapOf<PersonIdentifikator, Person>()

            override fun hent(ident: PersonIdentifikator): Person? =
                personer[ident].also {
                    logger.info { "Henter person" }
                }

            override fun lagre(person: Person) {
                personer[person.ident()] = person
                logger.info { "Lagrer person" }
            }
        }

    private val rapidsConnection =
        RapidApplication.Builder(RapidApplication.RapidApplicationConfig.fromEnv(config))
            .withKtorModule { vedtakApi(personRepository = personRepository) }.build()

    init {
        HendelseMediator(
            rapidsConnection = rapidsConnection,
            personMediator =
                PersonMediator(
                    personRepository = personRepository,
                    aktivitetsloggMediator = AktivitetsloggMediator(rapidsConnection),
                    behovMediator = BehovMediator(rapidsConnection),
                ),
            hendelseRepository = InMemoryMeldingRepository(),
        )

        rapidsConnection.register(this)
    }

    fun start() = rapidsConnection.start()

    fun stop() = rapidsConnection.stop()

    override fun onStartup(rapidsConnection: RapidsConnection) {
        logger.info { "Starter opp dp-vedtak" }
        Elveslurper(rapidsConnection)
    }

    private class Elveslurper(rapidsConnection: RapidsConnection): River.PacketListener {

        init {
            River(rapidsConnection).apply {
                validate {it.requireKey("@id")}
            }.register(this)
        }
        override fun onPacket(packet: JsonMessage, context: MessageContext) {
            logger.info { "Mottok pakke med id ${packet["@id"].asText()}" }
        }


        private companion object {
            val logger = KotlinLogging.logger {  }
        }

    }
}
