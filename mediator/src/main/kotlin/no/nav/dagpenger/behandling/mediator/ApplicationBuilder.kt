package no.nav.dagpenger.behandling.mediator

import mu.KotlinLogging
import no.nav.dagpenger.behandling.mediator.api.behandlingApi
import no.nav.dagpenger.behandling.mediator.melding.InMemoryMeldingRepository
import no.nav.dagpenger.behandling.modell.Person
import no.nav.dagpenger.behandling.modell.PersonIdentifikator
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection

internal class ApplicationBuilder(config: Map<String, String>) : RapidsConnection.StatusListener {
    companion object {
        private val logger = KotlinLogging.logger { }
    }

    private val personRepository =
        object : PersonRepository {
            private val personer = mutableMapOf<PersonIdentifikator, Person>()

            override fun hent(ident: PersonIdentifikator): Person? =
                personer[ident]

            override fun lagre(person: Person) {
                personer[person.ident()] = person
            }
        }

    private val rapidsConnection =
        RapidApplication.Builder(RapidApplication.RapidApplicationConfig.fromEnv(config))
            .withKtorModule { behandlingApi(personRepository = personRepository) }.build()

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
        logger.info { "Starter opp dp-behandling" }
    }
}
