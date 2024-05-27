package no.nav.dagpenger.opplysning

import io.kotest.matchers.shouldBe
import no.nav.dagpenger.opplysning.TestOpplysningstyper.a
import no.nav.dagpenger.opplysning.TestOpplysningstyper.b
import no.nav.dagpenger.opplysning.TestOpplysningstyper.c
import no.nav.dagpenger.opplysning.TestOpplysningstyper.dato1
import no.nav.dagpenger.opplysning.TestOpplysningstyper.desimaltall
import no.nav.dagpenger.opplysning.regel.enAv
import no.nav.dagpenger.opplysning.regel.oppslag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate

class RegelkjøringTest {
    @Test
    fun `Regelsett kan ikke inneholder flere regler som produserer samme opplysningstype`() {
        val regelsett1 =
            Regelsett("regelsett") {
                regel(a) { enAv(b) }
            }
        val regelsett2 =
            Regelsett("regelsett") {
                regel(a) { enAv(c) }
            }

        assertThrows<IllegalArgumentException> {
            Regelkjøring(1.mai, Opplysninger(), regelsett1, regelsett2)
        }
    }

    @Test
    fun `Regelsett kan inneholde kriterier for når de skal være med i en regelkjøring`() {
        val kravPåDagpenger =
            Regelsett("har krav på dagpenger") {
                regel(b) { enAv(a) }
            }
        val fastsettPeriode =
            Regelsett("fastsett periode") {
                skalVurderes { har(b) && finnOpplysning(b).verdi }

                regel(desimaltall) { oppslag(dato1) { 50.0 } }
            }

        val opplysninger = Opplysninger()
        Regelkjøring(1.mai, opplysninger, kravPåDagpenger, fastsettPeriode)

        opplysninger.leggTil(Faktum(a, false))
        opplysninger.leggTil(Faktum(dato1, LocalDate.now()))

        Regelkjøring(1.mai, opplysninger, kravPåDagpenger, fastsettPeriode).apply {
            evaluer()
        }

        opplysninger.har(desimaltall) shouldBe false

        // Fastsett desimaltall
        opplysninger.leggTil(Faktum(a, true))

        Regelkjøring(1.mai, opplysninger, kravPåDagpenger, fastsettPeriode).apply {
            evaluer()
        }

        opplysninger.har(desimaltall) shouldBe true
        opplysninger.finnOpplysning(desimaltall).verdi shouldBe 50.0
    }

    @Test
    fun `Regelsett kan inneholde rekursive regelsett med kriterier for om de skal med`() {
        val kravPåDagpenger =
            Regelsett("har krav på dagpenger") {
                regel(b) { enAv(a) }

                regelsett("fastsett periode") {
                    skalVurderes { har(b) && finnOpplysning(b).verdi }

                    regel(desimaltall) { oppslag(dato1) { 50.0 } }
                }
            }

        val opplysninger = Opplysninger()
        Regelkjøring(1.mai, opplysninger, kravPåDagpenger)

        opplysninger.leggTil(Faktum(a, false))
        opplysninger.leggTil(Faktum(dato1, LocalDate.now()))

        Regelkjøring(1.mai, opplysninger, kravPåDagpenger).apply {
            evaluer()
        }

        opplysninger.har(desimaltall) shouldBe false

        // Fastsett desimaltall
        opplysninger.leggTil(Faktum(a, true))

        Regelkjøring(1.mai, opplysninger, kravPåDagpenger).apply {
            evaluer()
        }

        opplysninger.har(desimaltall) shouldBe true
        opplysninger.finnOpplysning(desimaltall).verdi shouldBe 50.0
    }
}
