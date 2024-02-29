package no.nav.dagpenger.opplysning.regel.dato

import no.nav.dagpenger.opplysning.Hypotese
import no.nav.dagpenger.opplysning.Opplysninger
import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.opplysning.Regelkjøring
import no.nav.dagpenger.opplysning.Regelsett
import no.nav.dagpenger.opplysning.mai
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class FørsteArbeidsdagTest {
    private val dato =
        Opplysningstype.somDato("dato")
    private val arbeidsdag =
        Opplysningstype.somDato("arbeidsdag")

    private val opplysninger = Opplysninger()
    private val regelkjøring =
        Regelkjøring(
            1.mai,
            opplysninger,
            Regelsett("finn første arbeidsdag for en dato") {
                regel(arbeidsdag) { førsteArbeidsdag(dato) }
            },
        )

    @Test
    fun `Fredag 5 juli  2019 er en arbeidsag`() {
        opplysninger.leggTil(Hypotese(dato, LocalDate.of(2019, 7, 5)))
        val utledet = opplysninger.finnOpplysning(arbeidsdag)
        assertEquals(LocalDate.of(2019, 7, 5), utledet.verdi)
    }

    @Test
    fun `Fredag 5 mai  2019 er en søndag og dermed blir første arbeidsdag 6 mai`() {
        opplysninger.leggTil(Hypotese(dato, LocalDate.of(2019, 5, 5)))
        val utledet = opplysninger.finnOpplysning(arbeidsdag)
        assertEquals(LocalDate.of(2019, 5, 6), utledet.verdi)
    }

    @Test
    fun `Første arbeidsdag etter 5 oktober 2024 er mandag 7 oktober 2024`() {
        opplysninger.leggTil(Hypotese(dato, LocalDate.of(2024, 10, 5)))
        val utledet = opplysninger.finnOpplysning(arbeidsdag)
        assertEquals(LocalDate.of(2024, 10, 7), utledet.verdi)
    }
}
