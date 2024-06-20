package no.nav.dagpenger.behandling.mediator.repository

import io.kotest.matchers.shouldBe
import no.nav.dagpenger.avklaring.Avklaring
import no.nav.dagpenger.avklaring.Avklaringkode
import no.nav.dagpenger.behandling.db.Postgres.withMigratedDb
import no.nav.dagpenger.behandling.modell.Behandling
import no.nav.dagpenger.behandling.modell.Behandling.TilstandType
import no.nav.dagpenger.behandling.modell.hendelser.SøknadInnsendtHendelse
import no.nav.dagpenger.opplysning.Opplysninger
import no.nav.dagpenger.opplysning.UUIDv7
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime

class AvklaringRepositoryPostgresTest {
    private val rapid = TestRapid()
    private val repository = AvklaringRepositoryPostgres()

    @Test
    fun `lagrer og rehydrer avklaringer`() {
        withMigratedDb {
            val kode1 = Avklaringkode("JobbetUtenforNorge", "Arbeid utenfor Norge", "Personen har oppgitt arbeid utenfor Norge")
            val kode2 = Avklaringkode("HarVunnetNobelprisen", "Har vunnet nobelprisen", "Personen har vunnet en nobelpris ")

            val behandling = TestBehandling(avklaring(kode1), avklaring(kode2))
            val avklaringer = repository.hentAvklaringer(behandling.behandlingId)

            avklaringer.size shouldBe 2
        }
    }

    @Test
    fun `tar vare på rekkefølge av endringer`() {
        withMigratedDb {
            val kode1 = Avklaringkode("JobbetUtenforNorge", "Arbeid utenfor Norge", "Personen har oppgitt arbeid utenfor Norge")

            val avklaring = avklaring(kode1)
            avklaring.kvittering()
            avklaring.gjenåpne()
            avklaring.avbryt()

            val forventedeTilstander = listOf("UnderBehandling", "Avklart", "UnderBehandling", "Avbrutt")
            avklaring.endringer.map { it::class.simpleName!! } shouldBe forventedeTilstander

            val behandling = TestBehandling(avklaring)
            val avklaringer = repository.hentAvklaringer(behandling.behandlingId)

            avklaringer.single().endringer.map { it::class.simpleName!! } shouldBe forventedeTilstander
        }
    }

    private inner class TestBehandling(
        vararg avklaring: Avklaring,
    ) {
        val behandlingId get() = behandling.behandlingId
        private val behandling = behandling(*avklaring)
        private val behandlingRepository = BehandlingRepositoryPostgres(OpplysningerRepositoryPostgres(), repository)

        init {
            lagre()
        }

        fun lagre() {
            val unitOfWork = PostgresUnitOfWork.transaction()
            behandlingRepository.lagre(behandling, unitOfWork)
            repository.lagreAvklaringer(behandling, unitOfWork)
            unitOfWork.commit()
        }

        private fun behandling(vararg avklaring: Avklaring) =
            Behandling.rehydrer(
                behandlingId = UUIDv7.ny(),
                behandler = SøknadInnsendtHendelse(UUIDv7.ny(), "123", UUIDv7.ny(), LocalDate.now(), 1),
                gjeldendeOpplysninger = Opplysninger(emptyList()),
                basertPå = emptyList(),
                tilstand = TilstandType.UnderBehandling,
                sistEndretTilstand = LocalDateTime.now(),
                avklaringer = avklaring.toList(),
            )
    }

    private fun avklaring(avklaringkode: Avklaringkode) = Avklaring(UUIDv7.ny(), avklaringkode)
}
