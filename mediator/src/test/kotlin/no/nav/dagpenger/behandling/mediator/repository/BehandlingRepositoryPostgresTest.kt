package no.nav.dagpenger.behandling.mediator.repository

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import no.nav.dagpenger.avklaring.Avklaring
import no.nav.dagpenger.behandling.db.Postgres.withMigratedDb
import no.nav.dagpenger.behandling.modell.Behandling
import no.nav.dagpenger.behandling.modell.UUIDv7
import no.nav.dagpenger.behandling.modell.hendelser.SøknadInnsendtHendelse
import no.nav.dagpenger.opplysning.Faktum
import no.nav.dagpenger.opplysning.Opplysninger
import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.regel.Avklaringspunkter
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class BehandlingRepositoryPostgresTest {
    private val ident = "123456789011"
    private val søknadId = UUIDv7.ny()
    private val søknadInnsendtHendelse =
        SøknadInnsendtHendelse(
            søknadId = søknadId,
            ident = ident,
            meldingsreferanseId = søknadId,
            gjelderDato = LocalDate.now(),
            fagsakId = 1,
            opprettet = LocalDateTime.now(),
        )
    private val tidligereOpplysning = Faktum(Opplysningstype.somDesimaltall("tidligere-opplysning"), 1.0)
    private val basertPåBehandling =
        Behandling.rehydrer(
            behandlingId = UUIDv7.ny(),
            behandler = søknadInnsendtHendelse,
            gjeldendeOpplysninger = Opplysninger(listOf(tidligereOpplysning)),
            tilstand = Behandling.TilstandType.Ferdig,
            sistEndretTilstand = LocalDateTime.now(),
            avklaringer = emptyList(),
        )
    private val opplysning1 = Faktum(Opplysningstype.somDesimaltall("aktiv-opplysning1"), 1.0)
    private val opplysning2 = Faktum(Opplysningstype.somDesimaltall("aktiv-opplysning2"), 2.0)
    private val opplysning3 = Faktum(Opplysningstype.somBoolsk("aktiv-opplysning3"), false)

    private val avklaring = Avklaring(UUIDv7.ny(), Avklaringspunkter.JobbetUtenforNorge)

    private val behandling =
        Behandling.rehydrer(
            behandlingId = UUIDv7.ny(),
            behandler = søknadInnsendtHendelse,
            gjeldendeOpplysninger = Opplysninger(listOf(opplysning1, opplysning2, opplysning3)),
            basertPå = listOf(basertPåBehandling),
            tilstand = Behandling.TilstandType.UnderBehandling,
            sistEndretTilstand = LocalDateTime.now(),
            avklaringer = listOf(avklaring),
        )

    @Test
    fun `lagre og hent behandling fra postgres`() {
        withMigratedDb {
            val avklaringRepository = AvklaringRepositoryPostgres()
            val behandlingRepositoryPostgres = BehandlingRepositoryPostgres(OpplysningerRepositoryPostgres(), avklaringRepository)
            behandlingRepositoryPostgres.lagre(basertPåBehandling)
            behandlingRepositoryPostgres.lagre(behandling)
            val rehydrertBehandling = behandlingRepositoryPostgres.hentBehandling(behandling.behandlingId).shouldNotBeNull()
            rehydrertBehandling.behandlingId shouldBe behandling.behandlingId
            rehydrertBehandling.basertPå.size shouldBe behandling.basertPå.size

            rehydrertBehandling.basertPå shouldContainExactly behandling.basertPå
            rehydrertBehandling.opplysninger().finnAlle().size shouldBe behandling.opplysninger().finnAlle().size
            rehydrertBehandling.opplysninger().finnAlle() shouldContainExactly behandling.opplysninger().finnAlle()

            val avklaringer = avklaringRepository.hentAvklaringer(behandling.behandlingId)
            avklaringer.size shouldBe 1
            with(avklaringer.first()) {
                val førsteEndring = endringer.first()
                val sisteEndring = endringer.last()

                id shouldBe avklaring.id
                kode shouldBe avklaring.kode
                endringer.size shouldBe 2

                with(førsteEndring) {
                    javaClass.simpleName shouldBe
                        avklaring.endringer
                            .first()
                            .javaClass.simpleName
                    id shouldBe avklaring.endringer.first().id
                    endret.truncatedTo(ChronoUnit.SECONDS) shouldBe
                        avklaring.endringer
                            .first()
                            .endret
                            .truncatedTo(ChronoUnit.SECONDS)
                }

                with(sisteEndring) {
                    javaClass.simpleName shouldBe
                        avklaring.endringer
                            .last()
                            .javaClass.simpleName
                    id shouldBe avklaring.endringer.last().id
                    endret.truncatedTo(ChronoUnit.SECONDS) shouldBe
                        avklaring.endringer
                            .last()
                            .endret
                            .truncatedTo(ChronoUnit.SECONDS)
                }
            }
        }
    }
}
