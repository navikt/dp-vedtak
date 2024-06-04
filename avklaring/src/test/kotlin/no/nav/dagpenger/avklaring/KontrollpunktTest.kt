package no.nav.dagpenger.avklaring

import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import no.nav.dagpenger.avklaring.Kontrollpunkt.Kontrollresultat
import no.nav.dagpenger.avklaring.KontrollpunktTest.TestAvklaringer.ArbeidIEØS
import no.nav.dagpenger.avklaring.KontrollpunktTest.TestAvklaringer.TestIkke123
import no.nav.dagpenger.dato.mai
import no.nav.dagpenger.opplysning.Faktum
import no.nav.dagpenger.opplysning.Opplysninger
import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.opplysning.Regelkjøring
import org.junit.jupiter.api.Test

class KontrollpunktTest {
    enum class TestAvklaringer(
        override val kode: String,
        override val tittel: String,
        override val beskrivelse: String,
    ) : Avklaringkode {
        ArbeidIEØS("ArbeidIEØS", "Arbeid i EØS", "Krever avklaring om arbeid i EØS"),
        TestIkke123("TestIkke123", "Test må være 123", "Krever avklaring om hvorfor test ikke er 123"),
    }

    private val opplysningstype = Opplysningstype.somHeltall("test")

    private fun getOpplysning(verdi: Int) = Faktum(opplysningstype, verdi)

    @Test
    fun `lager kontroller`() {
        val kontrollpunkt =
            Kontrollpunkt { opplysninger ->
                when (opplysninger.finnAlle().isEmpty()) {
                    true -> Kontrollresultat.OK
                    false -> Kontrollresultat.KreverAvklaring(ArbeidIEØS)
                }
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
                Kontrollpunkt { opplysninger ->
                    when (opplysninger.finnOpplysning(opplysningstype).verdi) {
                        123 -> Kontrollresultat.OK
                        else -> Kontrollresultat.KreverAvklaring(TestIkke123)
                    }
                },
            )

        val opplysninger = Opplysninger().also { Regelkjøring(1.mai(2024), it) }
        opplysninger.leggTil(getOpplysning(321))

        val ding = Dingseboms(kontrollpunkter)
        ding.avklaringer(opplysninger).also { avklaringer ->
            avklaringer.all { it.måAvklares() } shouldBe true
            avklaringer.size shouldBe 1
        }

        opplysninger.leggTil(getOpplysning(123))
        ding.avklaringer(opplysninger).also { avklaringer ->
            avklaringer.size shouldBe 0
            // TODO: Bør returnere også avklarte/avbrutte
            // avklaringer.all { it.måAvklares() } shouldBe false
        }
    }
}

class Dingseboms(private val kontrollpunkter: List<Kontrollpunkt>) {
    fun avklaringer(opplysninger: Opplysninger): List<Avklaring> {
        return kontrollpunkter.mapNotNull { kontrollpunkt ->
            when (val resultat = kontrollpunkt.evaluer(opplysninger)) {
                is Kontrollresultat.KreverAvklaring -> resultat.avklaring
                else -> null
            }
        }
    }
}
