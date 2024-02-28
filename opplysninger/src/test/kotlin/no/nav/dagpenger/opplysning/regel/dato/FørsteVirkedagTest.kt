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

class FørsteVirkedagTest {
    private val dato =
        Opplysningstype.somDato("dato")
    private val virkedag =
        Opplysningstype.somDato("virkedag")

    private val opplysninger = Opplysninger()
    private val regelkjøring =
        Regelkjøring(
            1.mai,
            opplysninger,
            Regelsett("regelsett") {
                regel(virkedag) { førsteVirkedag(dato) }
            },
        )

    @Test
    fun `Fredag 7 juli  2019 er en virkedag`() {
        opplysninger.leggTil(Hypotese(dato, LocalDate.of(2019, 7, 5)))
        val utledet = opplysninger.finnOpplysning(virkedag)
        assertEquals(LocalDate.of(2019, 7, 5), utledet.verdi)
    }

    @Test
    fun `Første virkedag etter 5 oktober 2019 er mandag 7 oktober 2019`() {
        opplysninger.leggTil(Hypotese(dato, LocalDate.of(2019, 10, 5)))
        val utledet = opplysninger.finnOpplysning(virkedag)
        assertEquals(LocalDate.of(2019, 10, 7), utledet.verdi)
    }
}
