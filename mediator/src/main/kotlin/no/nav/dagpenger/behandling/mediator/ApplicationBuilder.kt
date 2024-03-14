package no.nav.dagpenger.behandling.mediator

import mu.KotlinLogging
import no.nav.dagpenger.behandling.db.PostgresDataSourceBuilder
import no.nav.dagpenger.behandling.db.PostgresDataSourceBuilder.clean
import no.nav.dagpenger.behandling.db.PostgresDataSourceBuilder.runMigration
import no.nav.dagpenger.behandling.mediator.api.behandlingApi
import no.nav.dagpenger.behandling.mediator.melding.PostgresHendelseRepository
import no.nav.dagpenger.behandling.mediator.repository.OpplysningRepositoryPostgres
import no.nav.dagpenger.behandling.modell.Behandling
import no.nav.dagpenger.behandling.modell.Person
import no.nav.dagpenger.behandling.modell.PersonIdentifikator
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection
import java.util.UUID

internal class ApplicationBuilder(config: Map<String, String>) : RapidsConnection.StatusListener {
    companion object {
        private val logger = KotlinLogging.logger { }
    }

    private val opplysningRepository = OpplysningRepositoryPostgres()
    private val personRepository =
        object : PersonRepository, BehandlingRepository {
            private val personer = mutableMapOf<PersonIdentifikator, Person>()

            override fun hent(ident: PersonIdentifikator): Person? = personer[ident]

            override fun lagre(person: Person) {
                personer[person.ident()] = person
                opplysningRepository.lagreOpplysninger(person.behandlinger().flatMap { it.opplysninger().finnAlle() })
            }

            override fun hent(behandlingId: UUID): Behandling? {
                return personer.values.flatMap { it.behandlinger() }.find { it.behandlingId == behandlingId }
            }
        }

    private val rapidsConnection =
        RapidApplication.Builder(RapidApplication.RapidApplicationConfig.fromEnv(config))
            .withKtorModule { behandlingApi(personRepository = personRepository) }.build()

    init {
        MessageMediator(
            rapidsConnection = rapidsConnection,
            personMediator =
                PersonMediator(
                    personRepository = personRepository,
                    aktivitetsloggMediator = AktivitetsloggMediator(rapidsConnection),
                    behovMediator = BehovMediator(rapidsConnection),
                    hendelseMediator = HendelseMediator(rapidsConnection),
                    observatører = emptySet(),
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
        logger.info { "Starter opp dp-behandling" }
    }
}
