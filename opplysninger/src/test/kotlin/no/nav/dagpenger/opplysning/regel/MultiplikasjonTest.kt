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
        opplysninger.leggTil(Faktum(beløpB, Beløp(2.0)))
        opplysninger.leggTil(Faktum(faktorB, 2.0))
        regelkjøring.evaluer()
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
        opplysninger.leggTil(Faktum(beløpB, Beløp(2.0)))
        opplysninger.leggTil(Faktum(heltallA, 2))
        regelkjøring.evaluer()
        val utledet = opplysninger.finnOpplysning(beløpA)
        assertEquals(Beløp(4.0), utledet.verdi)
    }
}
