package no.nav.dagpenger.behandling.modell.hendelser

import io.kotest.matchers.shouldBe
import no.nav.dagpenger.behandling.modell.Ident
import no.nav.dagpenger.behandling.modell.Person
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class MeldekortMottattHendelseTest {
    @Test
    fun `Kan motta meldekort og opprette meldekort på person`() {
        val meldekortMottattHendelse =
            MeldekortMottattHendelse(
                meldingsreferanseId = UUID.randomUUID(),
                ident = "12345678901",
                meldekortId = UUID.randomUUID(),
                meldekortFraOgmed = LocalDate.now().minusDays(14),
                meldekortTilOgmed = LocalDate.now(),
                arbeidsdager =
                    mapOf(
                        LocalDate.now().minusDays(14) to 7,
                        LocalDate.now().minusDays(13) to 7,
                        LocalDate.now().minusDays(12) to 7,
                        LocalDate.now().minusDays(11) to 7,
                        LocalDate.now().minusDays(10) to 7,
                        LocalDate.now().minusDays(9) to 7,
                        LocalDate.now().minusDays(8) to 7,
                    ),
                opprettet = LocalDateTime.now(),
            )

        val person = Person(ident = Ident("12345678901"))
        person.håndter(meldekortMottattHendelse)
        person.håndter(meldekortMottattHendelse)

        person.meldekort.size shouldBe 2
    }
}
