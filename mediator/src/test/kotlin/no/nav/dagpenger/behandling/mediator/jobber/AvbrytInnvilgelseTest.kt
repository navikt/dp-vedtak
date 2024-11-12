package no.nav.dagpenger.behandling.mediator.jobber

import com.github.navikt.tbd_libs.rapids_and_rivers.test_support.TestRapid
import io.kotest.matchers.shouldBe
import no.nav.dagpenger.behandling.db.Postgres.withMigratedDb
import no.nav.dagpenger.behandling.mediator.repository.AvklaringRepositoryPostgres
import no.nav.dagpenger.behandling.mediator.repository.BehandlingRepositoryPostgres
import no.nav.dagpenger.behandling.mediator.repository.OpplysningerRepositoryPostgres
import no.nav.dagpenger.behandling.mediator.repository.PersonRepositoryPostgres
import no.nav.dagpenger.behandling.modell.Behandling
import no.nav.dagpenger.behandling.modell.Behandling.TilstandType.Ferdig
import no.nav.dagpenger.behandling.modell.Behandling.TilstandType.ForslagTilVedtak
import no.nav.dagpenger.behandling.modell.Behandling.TilstandType.UnderBehandling
import no.nav.dagpenger.behandling.modell.Ident.Companion.tilPersonIdentfikator
import no.nav.dagpenger.behandling.modell.Person
import no.nav.dagpenger.opplysning.Faktum
import no.nav.dagpenger.opplysning.Opplysninger
import no.nav.dagpenger.regel.KravPåDagpenger.kravPåDagpenger
import no.nav.dagpenger.regel.SøknadInnsendtHendelse
import no.nav.dagpenger.uuid.UUIDv7
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime

class AvbrytInnvilgelseTest {
    private val rapid = TestRapid()

    @Test
    fun `avbryter behandlinger eldre enn tre dager med forslag om innvilgelse`() =
        withMigratedDb {
            val avbrytInnvilgelse = AvbrytInnvilgelse(rapid)

            lagTestData()

            avbrytInnvilgelse.avbrytBehandlinger(3)

            rapid.inspektør.size shouldBe 1
        }

    private val søknadInnsendtHendelse =
        SøknadInnsendtHendelse(
            meldingsreferanseId = UUIDv7.ny(),
            ident = "12312312311",
            søknadId = UUIDv7.ny(),
            gjelderDato = LocalDate.now(),
            fagsakId = 1,
            opprettet = LocalDateTime.now(),
        )

    private fun lagTestData() {
        val personRepository =
            PersonRepositoryPostgres(
                BehandlingRepositoryPostgres(
                    OpplysningerRepositoryPostgres(),
                    AvklaringRepositoryPostgres(),
                ),
            )
        val person =
            Person(
                "12312312311".tilPersonIdentfikator(),
                listOf(
                    // Under behandling skal ikke med
                    Behandling.rehydrer(
                        behandlingId = UUIDv7.ny(),
                        behandler =
                        søknadInnsendtHendelse,
                        gjeldendeOpplysninger =
                            Opplysninger(
                                listOf(Faktum(kravPåDagpenger, true)),
                            ),
                        basertPå = emptyList(),
                        tilstand = UnderBehandling,
                        sistEndretTilstand = LocalDateTime.now().minusDays(5),
                        avklaringer = emptyList(),
                    ),
                    // Forslag til vedtak skal med
                    Behandling.rehydrer(
                        behandlingId = UUIDv7.ny(),
                        behandler = søknadInnsendtHendelse,
                        gjeldendeOpplysninger =
                            Opplysninger(
                                listOf(Faktum(kravPåDagpenger, true)),
                            ),
                        basertPå = emptyList(),
                        tilstand = ForslagTilVedtak,
                        sistEndretTilstand = LocalDateTime.now().minusDays(5),
                        avklaringer = emptyList(),
                    ),
                    // Forslag til vedtak hvor utfall er false
                    Behandling.rehydrer(
                        behandlingId = UUIDv7.ny(),
                        behandler = søknadInnsendtHendelse,
                        gjeldendeOpplysninger =
                            Opplysninger(
                                listOf(Faktum(kravPåDagpenger, false)),
                            ),
                        basertPå = emptyList(),
                        tilstand = ForslagTilVedtak,
                        sistEndretTilstand = LocalDateTime.now().minusDays(5),
                        avklaringer = emptyList(),
                    ),
                    // Forslag til vedtak som ikke er innvilgelse
                    Behandling.rehydrer(
                        behandlingId = UUIDv7.ny(),
                        behandler = søknadInnsendtHendelse,
                        gjeldendeOpplysninger =
                            Opplysninger(),
                        basertPå = emptyList(),
                        tilstand = ForslagTilVedtak,
                        sistEndretTilstand = LocalDateTime.now().minusDays(5),
                        avklaringer = emptyList(),
                    ),
                    // Forslag til vedtak som er for nytt skal ikke med
                    Behandling.rehydrer(
                        behandlingId = UUIDv7.ny(),
                        behandler = søknadInnsendtHendelse,
                        gjeldendeOpplysninger =
                            Opplysninger(
                                listOf(Faktum(kravPåDagpenger, true)),
                            ),
                        basertPå = emptyList(),
                        tilstand = ForslagTilVedtak,
                        sistEndretTilstand = LocalDateTime.now(),
                        avklaringer = emptyList(),
                    ),
                    // Ferdige behandlinger skal ikke med
                    Behandling.rehydrer(
                        behandlingId = UUIDv7.ny(),
                        behandler = søknadInnsendtHendelse,
                        gjeldendeOpplysninger =
                            Opplysninger(
                                listOf(Faktum(kravPåDagpenger, true)),
                            ),
                        basertPå = emptyList(),
                        tilstand = Ferdig,
                        sistEndretTilstand = LocalDateTime.now().minusDays(5),
                        avklaringer = emptyList(),
                    ),
                ),
            )
        personRepository.lagre(person)
    }
}
