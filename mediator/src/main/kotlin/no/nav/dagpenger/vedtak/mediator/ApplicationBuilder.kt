package no.nav.dagpenger.vedtak.mediator

import mu.KotlinLogging
import no.nav.dagpenger.vedtak.db.PostgresDataSourceBuilder
import no.nav.dagpenger.vedtak.db.PostgresDataSourceBuilder.clean
import no.nav.dagpenger.vedtak.db.PostgresDataSourceBuilder.runMigration
import no.nav.dagpenger.vedtak.mediator.api.vedtakApi
import no.nav.dagpenger.vedtak.mediator.persistens.PersonRepository
import no.nav.dagpenger.vedtak.mediator.persistens.PostgresHendelseRepository
import no.nav.dagpenger.vedtak.modell.Person
import no.nav.dagpenger.vedtak.modell.PersonIdentifikator
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection

internal class ApplicationBuilder(config: Map<String, String>) : RapidsConnection.StatusListener {
    companion object {
        private val logger = KotlinLogging.logger { }
    }

    private val personRepository =
        object : PersonRepository {
            private val personer = mutableMapOf<PersonIdentifikator, Person>()

            override fun hent(ident: PersonIdentifikator): Person? = personer[ident]

            override fun lagre(person: Person) {
                personer[person.ident()] = person
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
            hendelseRepository = PostgresHendelseRepository(PostgresDataSourceBuilder.dataSource),
        )

        rapidsConnection.register(this)
    }

    fun start() = rapidsConnection.start()

    fun stop() = rapidsConnection.stop()

    override fun onStartup(rapidsConnection: RapidsConnection) {
        clean()
        runMigration()
        logger.info { "Starter opp dp-vedtak" }
    }
}
