package no.nav.dagpenger.opplysning.regel

import no.nav.dagpenger.opplysning.Faktum
import no.nav.dagpenger.opplysning.Opplysninger
import no.nav.dagpenger.opplysning.Regelkjøring
import no.nav.dagpenger.opplysning.Regelsett
import no.nav.dagpenger.opplysning.TestOpplysningstyper.faktorA
import no.nav.dagpenger.opplysning.TestOpplysningstyper.faktorB
import no.nav.dagpenger.opplysning.TestOpplysningstyper.produkt
import no.nav.dagpenger.opplysning.mai
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class MultiplikasjonTest {
    private val opplysninger = Opplysninger()
    private val regelkjøring =
        Regelkjøring(
            1.mai,
            opplysninger,
            Regelsett("regelsett") {
                regel(produkt) { multiplikasjon(faktorA, faktorB) }
            },
        )

    @Test
    fun `multiplikasjon regel`() {
        opplysninger.leggTil(Faktum(faktorA, 2.0))
        opplysninger.leggTil(Faktum(faktorB, 2.0))
        val utledet = opplysninger.finnOpplysning(produkt)
        assertEquals(4.0, utledet.verdi)
    }
}
