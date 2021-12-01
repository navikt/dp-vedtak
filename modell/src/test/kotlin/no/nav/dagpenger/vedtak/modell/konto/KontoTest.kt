package no.nav.dagpenger.vedtak.modell.konto

import no.nav.dagpenger.vedtak.modell.mengder.Enhet.Companion.arbeidsdager
import no.nav.dagpenger.vedtak.modell.mengder.Enhet.Companion.ukeprosent
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate

internal class KontoTest {
    private lateinit var konto: Konto

    @BeforeEach
    fun setup() {
        konto = Konto().apply {
            leggTilPostering(Postering(50.arbeidsdager, LocalDate.now().minusMonths(1)))
            leggTilPostering(Postering(50.arbeidsdager, LocalDate.now().minusWeeks(1)))
            leggTilPostering(Postering(50.arbeidsdager, LocalDate.now().minusDays(1)))
        }
    }

    @Test
    fun `Kan få ut nåværende balanse`() {
        assertEquals(150.arbeidsdager, konto.balanse())
    }

    @Test
    fun `Kan beregne balansen på en gitt dato`() {
        assertEquals(
            100.arbeidsdager,
            konto.balanse(
                tilOgMed = LocalDate.now().minusDays(5)
            )
        )

        assertEquals(
            0.arbeidsdager,
            konto.balanse(
                tilOgMed = LocalDate.now().minusMonths(5)
            )
        )

        assertEquals(
            150.arbeidsdager,
            konto.balanse(
                tilOgMed = LocalDate.now().plusMonths(5)
            )
        )
    }

    @Test
    fun `Kan beregne balansen i ett gitt tidsrom`() {
        assertEquals(
            50.arbeidsdager,
            konto.balanse(
                fraOgMed = LocalDate.now().minusMonths(5),
                tilOgMed = LocalDate.now().minusWeeks(2),
            )
        )
    }

    @Test
    @Disabled
    fun `Kan holde på ulike enheter, men kun en type`() {
        Konto().apply {
            leggTilPostering(Postering(5.arbeidsdager, LocalDate.now()))

            assertThrows<Exception> {
                leggTilPostering(Postering(5.ukeprosent, LocalDate.now()))
            }
        }
    }
}
