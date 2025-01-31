package no.nav.dagpenger.behandling.mediator.repository

import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.mockk
import kotliquery.sessionOf
import no.nav.dagpenger.behandling.db.Postgres.withMigratedDb
import no.nav.dagpenger.behandling.db.PostgresDataSourceBuilder.dataSource
import no.nav.dagpenger.behandling.mediator.melding.PostgresHendelseRepository
import no.nav.dagpenger.opplysning.Saksbehandler
import no.nav.dagpenger.opplysning.Saksbehandlerkilde
import org.junit.jupiter.api.Test
import java.util.UUID

class KildeRepositoryTest {
    private val meldingsreferanseId = UUID.randomUUID()
    private val kildeRepository = KildeRepository()

    init {
        PostgresHendelseRepository().also {
            it.lagreMelding(
                mockk(),
                "123456789",
                meldingsreferanseId,
                "{}",
            )
        }
    }

    @Test
    fun lagreBegrunnelse() =
        withMigratedDb {
            val kilde = Saksbehandlerkilde(meldingsreferanseId, Saksbehandler("EIF2025"))

            kildeRepository.lagreKilde(kilde, sessionOf(dataSource))
            kildeRepository.lagreBegrunnelse(kilde.id, "Begrunnelse")

            with(kildeRepository.hentKilde(kilde.id)) {
                shouldBeInstanceOf<Saksbehandlerkilde>()

                begrunnelse?.verdi shouldBe "Begrunnelse"
            }
        }
}
