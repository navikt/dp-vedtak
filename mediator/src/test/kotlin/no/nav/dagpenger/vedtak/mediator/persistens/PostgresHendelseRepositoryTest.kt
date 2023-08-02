package no.nav.dagpenger.vedtak.mediator.persistens

import io.kotest.matchers.equals.shouldBeEqual
import no.nav.dagpenger.vedtak.db.Postgres
import no.nav.dagpenger.vedtak.db.PostgresDataSourceBuilder
import no.nav.dagpenger.vedtak.mediator.Meldingsfabrikk
import no.nav.dagpenger.vedtak.mediator.mottak.SøknadBehandletHendelseMessage
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageProblems
import org.junit.jupiter.api.Test

internal class PostgresHendelseRepositoryTest {

    @Test
    fun `lagre og hent hendelse`() {
        val hendelse = Meldingsfabrikk.dagpengerInnvilgetJson()
        val jsonMessage = JsonMessage(hendelse, MessageProblems(hendelse)).also {
            it.interestedIn("@id", "ident")
        }
        val søknadBehandletHendelseMessage = SøknadBehandletHendelseMessage(jsonMessage)

        Postgres.withMigratedDb {
            val postgresHendelseRepository = PostgresHendelseRepository(PostgresDataSourceBuilder.dataSource)
            postgresHendelseRepository.lagreMelding(
                hendelseMessage = søknadBehandletHendelseMessage,
                ident = søknadBehandletHendelseMessage.hentIdent(),
                id = søknadBehandletHendelseMessage.id,
                toJson = hendelse,
            )
            postgresHendelseRepository.erBehandlet(søknadBehandletHendelseMessage.id) shouldBeEqual false
            postgresHendelseRepository.markerSomBehandlet(søknadBehandletHendelseMessage.id)
            postgresHendelseRepository.erBehandlet(søknadBehandletHendelseMessage.id) shouldBeEqual true
        }
    }
}
