package no.nav.dagpenger.behandling.mediator.repository

import no.nav.dagpenger.behandling.db.Postgres.withMigratedDb
import no.nav.dagpenger.behandling.modell.Behandling
import no.nav.dagpenger.behandling.modell.UUIDv7
import no.nav.dagpenger.behandling.modell.hendelser.SøknadInnsendtHendelse
import no.nav.dagpenger.opplysning.Opplysningstype
import org.junit.jupiter.api.Test
import java.time.LocalDate

class BehandlingRepositoryPostgresTest {
    private val ident = "123456789011"
    private val søknadId = UUIDv7.ny()
    private val søknadInnsendtHendelse =
        SøknadInnsendtHendelse(
            søknadId = søknadId,
            ident = ident,
            meldingsreferanseId = søknadId,
            gjelderDato = LocalDate.now(),
        )
    private val tidligereOpplysning = Opplysningstype.somDesimaltall("opplysning-fra-tidligere-behandling")

    @Test
    fun `hent behandling fra postgres`() {
        val behandling =
            Behandling(
                behandler = søknadInnsendtHendelse,
                opplysninger = emptyList(),
            )
        withMigratedDb {
            val behandlingRepositoryPostgres = BehandlingRepositoryPostgres()
            behandlingRepositoryPostgres.lagre(behandling)
            val hentetBehandling = behandlingRepositoryPostgres.hent(behandling.behandlingId)
        }
    }
}
