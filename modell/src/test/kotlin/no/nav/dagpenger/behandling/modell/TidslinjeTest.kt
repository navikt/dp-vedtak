package no.nav.dagpenger.behandling.modell

import io.kotest.matchers.collections.shouldContainInOrder
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID

class TidslinjeTest {
    @Test
    fun `automatisk og riktig sortering av hendelser`() {
        val nå = LocalDateTime.now()
        val hendelser =
            listOf(
                Tidslinjehendelse(UUID.randomUUID(), Meldingstype.Søknad, nå.minusDays(4)),
                Tidslinjehendelse(UUID.randomUUID(), Meldingstype.Meldekort, nå.minusDays(3)),
                Tidslinjehendelse(UUID.randomUUID(), Meldingstype.Meldekort, nå.minusDays(2)),
                Tidslinjehendelse(UUID.randomUUID(), Meldingstype.Søknad, nå.minusDays(1)),
            )

        val tidslinje = Tidslinje(hendelser[3], hendelser[2], hendelser[0])
        tidslinje.leggTilHendelse(hendelser[1])

        tidslinje.shouldContainInOrder(hendelser)

        tidslinje.nesteTilBehandling() shouldBe hendelser[0]
        tidslinje.harUbehandledeHendelser() shouldBe true
    }
}
