package no.nav.dagpenger.avklaring

import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import no.nav.dagpenger.avklaring.Kontrollpunkt.Kontrollresultat
import no.nav.dagpenger.avklaring.TestAvklaringer.ArbeidIEØS
import no.nav.dagpenger.avklaring.TestAvklaringer.TestIkke123
import no.nav.dagpenger.dato.mai
import no.nav.dagpenger.opplysning.Faktum
import no.nav.dagpenger.opplysning.LesbarOpplysninger
import no.nav.dagpenger.opplysning.Opplysninger
import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.opplysning.Regelkjøring
import org.junit.jupiter.api.Test

class KontrollpunktTest {
    private val opplysningstype = Opplysningstype.somHeltall("test")

    private fun getOpplysning(verdi: Int) = Faktum(opplysningstype, verdi)

    @Test
    fun `lager kontroller`() {
        val kontrollpunkt =
            Kontrollpunkt(ArbeidIEØS) { opplysninger ->
                opplysninger.finnAlle().isEmpty()
            }

        val opplysninger = Opplysninger().also { Regelkjøring(1.mai(2024), it) }
        with(kontrollpunkt.evaluer(opplysninger)) {
            this shouldBe Kontrollresultat.OK
        }

        opplysninger.leggTil(getOpplysning(123))
        with(kontrollpunkt.evaluer(opplysninger)) {
            this.shouldBeInstanceOf<Kontrollresultat.KreverAvklaring>()

            this.avklaring.kode shouldBe ArbeidIEØS
        }
    }

    @Test
    fun `avklaringer avklares`() {
        val kontrollpunkter =
            listOf(
                Kontrollpunkt(TestIkke123) { opplysninger ->
                    opplysninger.finnOpplysning(opplysningstype).verdi == 123
                },
            )

        val opplysninger = Opplysninger().also { Regelkjøring(1.mai(2024), it) }
        opplysninger.leggTil(getOpplysning(321))

        val ding = Dingseboms(kontrollpunkter)
        ding.avklaringer(opplysninger).also { avklaringer ->
            avklaringer.size shouldBe 1
            avklaringer.all { it.måAvklares() } shouldBe true
        }

        opplysninger.leggTil(getOpplysning(123))
        ding.avklaringer(opplysninger).also { avklaringer ->
            avklaringer.size shouldBe 1
            avklaringer.all { it.måAvklares() } shouldBe false
        }
    }
}

class Dingseboms(private val kontrollpunkter: List<Kontrollpunkt>, avklaringer: List<Avklaring> = emptyList()) {
    private val avklaringer = avklaringer.toMutableSet()

    fun avklaringer(opplysninger: LesbarOpplysninger): List<Avklaring> {
        val aktiveAvklaringer =
            kontrollpunkter
                .map { it.evaluer(opplysninger) }
                .filterIsInstance<Kontrollresultat.KreverAvklaring>()
                .map { it.avklaring }

        // Avbryt alle avklaringer som ikke lenger er aktive
        avklaringer
            .filter { it.måAvklares() }
            .filterNot { avklaring: Avklaring -> aktiveAvklaringer.contains(avklaring) }
            .forEach { it.avbryt() }

        avklaringer.addAll(aktiveAvklaringer)

        return avklaringer.toList()
    }
}
