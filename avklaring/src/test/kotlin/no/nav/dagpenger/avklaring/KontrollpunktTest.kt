package no.nav.dagpenger.avklaring

import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import no.nav.dagpenger.avklaring.Kontrollpunkt.Kontrollresultat
import no.nav.dagpenger.avklaring.KontrollpunktTest.TestAvklaringer.ArbeidIEØS
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
    }

    private val opplysning = Faktum(Opplysningstype.somHeltall("test"), 123)

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

        opplysninger.leggTil(opplysning)
        with(kontrollpunkt.evaluer(opplysninger)) {
            this.shouldBeInstanceOf<Kontrollresultat.KreverAvklaring>()

            this.avklaring.kode shouldBe ArbeidIEØS
        }
    }
}
