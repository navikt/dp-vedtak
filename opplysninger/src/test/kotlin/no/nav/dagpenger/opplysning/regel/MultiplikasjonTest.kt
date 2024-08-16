package no.nav.dagpenger.opplysning.regel

import no.nav.dagpenger.opplysning.Faktum
import no.nav.dagpenger.opplysning.Opplysninger
import no.nav.dagpenger.opplysning.Regelkjøring
import no.nav.dagpenger.opplysning.Regelsett
import no.nav.dagpenger.opplysning.TestOpplysningstyper.beløpA
import no.nav.dagpenger.opplysning.TestOpplysningstyper.beløpB
import no.nav.dagpenger.opplysning.TestOpplysningstyper.faktorB
import no.nav.dagpenger.opplysning.TestOpplysningstyper.heltallA
import no.nav.dagpenger.opplysning.mai
import no.nav.dagpenger.opplysning.verdier.Beløp
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class MultiplikasjonTest {
    private val opplysninger = Opplysninger()

    @Test
    fun `multiplikasjon av double regel`() {
        val regelkjøring =
            Regelkjøring(
                1.mai,
                opplysninger,
                Regelsett("regelsett") {
                    regel(beløpA) { multiplikasjon(beløpB, faktorB) }
                },
            )
        regelkjøring.leggTil(Faktum(beløpB, Beløp(2.0)))
        regelkjøring.leggTil(Faktum(faktorB, 2.0))
        val utledet = opplysninger.finnOpplysning(beløpA)
        assertEquals(Beløp(4.0), utledet.verdi)
    }

    @Test
    fun `multiplikasjon av int regel`() {
        val regelkjøring =
            Regelkjøring(
                1.mai,
                opplysninger,
                Regelsett("regelsett") {
                    regel(beløpA) { multiplikasjon(beløpB, heltallA) }
                },
            )
        regelkjøring.leggTil(Faktum(beløpB, Beløp(2.0)))
        regelkjøring.leggTil(Faktum(heltallA, 2))
        val utledet = opplysninger.finnOpplysning(beløpA)
        assertEquals(Beløp(4.0), utledet.verdi)
    }
}
