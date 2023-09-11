package no.nav.dagpenger.vedtak.mediator.persistens

import io.kotest.matchers.equals.shouldBeEqual
import no.nav.dagpenger.vedtak.db.Postgres
import no.nav.dagpenger.vedtak.db.PostgresDataSourceBuilder
import no.nav.dagpenger.vedtak.mediator.Meldingsfabrikk
import no.nav.dagpenger.vedtak.mediator.mottak.RettighetBehandletHendelseMessage
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageProblems
import org.junit.jupiter.api.Test

internal class PostgresHendelseRepositoryTest {

    @Test
    fun `lagre og hent hendelse`() {
        val hendelse = Meldingsfabrikk.rettighetBehandletOgInnvilgetJson()
        val jsonMessage = JsonMessage(hendelse, MessageProblems(hendelse)).also {
            it.interestedIn("@id", "ident")
        }
        val rettighetBehandletHendelseMessage = RettighetBehandletHendelseMessage(jsonMessage)

        Postgres.withMigratedDb {
            val postgresHendelseRepository = PostgresHendelseRepository(PostgresDataSourceBuilder.dataSource)
            postgresHendelseRepository.lagreMelding(
                hendelseMessage = rettighetBehandletHendelseMessage,
                ident = rettighetBehandletHendelseMessage.hentIdent(),
                id = rettighetBehandletHendelseMessage.id,
                toJson = hendelse,
            )
            postgresHendelseRepository.erBehandlet(rettighetBehandletHendelseMessage.id) shouldBeEqual false
            postgresHendelseRepository.markerSomBehandlet(rettighetBehandletHendelseMessage.id)
            postgresHendelseRepository.erBehandlet(rettighetBehandletHendelseMessage.id) shouldBeEqual true
        }
    }
}
