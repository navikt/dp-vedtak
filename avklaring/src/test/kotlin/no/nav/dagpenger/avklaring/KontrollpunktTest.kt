package no.nav.dagpenger.avklaring

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.date.shouldBeAfter
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import no.nav.dagpenger.avklaring.Kontrollpunkt.Kontrollresultat
import no.nav.dagpenger.avklaring.TestAvklaringer.ArbeidIEØS
import no.nav.dagpenger.avklaring.TestAvklaringer.BeregningsregelForFVA
import no.nav.dagpenger.avklaring.TestAvklaringer.SvangerskapsrelaterteSykepenger
import no.nav.dagpenger.avklaring.TestAvklaringer.TestIkke123
import no.nav.dagpenger.dato.mai
import no.nav.dagpenger.opplysning.Faktum
import no.nav.dagpenger.opplysning.Opplysning
import no.nav.dagpenger.opplysning.Opplysninger
import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.opplysning.Regelkjøring
import org.junit.jupiter.api.Test

class KontrollpunktTest {
    companion object {
        private val opplysningstype = Opplysningstype.somHeltall("test")
        private val inntekterInneholderSykepenger = Opplysningstype.somBoolsk("Inntektene innholder sykepenger")
    }

    private fun getOpplysning(verdi: Int) = Faktum(opplysningstype, verdi)

    @Test
    fun `lager kontroller`() {
        val kontrollpunkt =
            Kontrollpunkt(ArbeidIEØS) { opplysninger ->
                opplysninger.har(opplysningstype) && opplysninger.finnOpplysning(opplysningstype).verdi == 123
            }

        val opplysninger = Opplysninger()
        val regelkjøring = Regelkjøring(1.mai(2024), opplysninger)

        with(kontrollpunkt.evaluer(opplysninger)) {
            this shouldBe Kontrollresultat.OK
        }

        regelkjøring.leggTil(getOpplysning(123) as Opplysning<*>)
        with(kontrollpunkt.evaluer(opplysninger)) {
            this.shouldBeInstanceOf<Kontrollresultat.KreverAvklaring>()

            this.avklaringkode shouldBe ArbeidIEØS
        }
    }

    @Test
    fun `avklaringer gjenåpnes når grunnlaget endres`() {
        val kontrollpunkter =
            listOf(
                Kontrollpunkt(TestIkke123) { opplysninger ->
                    opplysninger.har(opplysningstype) && opplysninger.finnOpplysning(opplysningstype).verdi == 321
                },
            )

        val opplysninger = Opplysninger()
        val regelkjøring = Regelkjøring(1.mai(2024), opplysninger)
        regelkjøring.leggTil(getOpplysning(321) as Opplysning<*>)

        val ding = Avklaringer(kontrollpunkter)
        ding.måAvklares(opplysninger).also { avklaringer ->
            avklaringer.size shouldBe 1
            avklaringer.all { it.måAvklares() } shouldBe true
            avklaringer.all { it.kode == TestIkke123 } shouldBe true
        }

        // Saksbehandler endrer opplysningen
        regelkjøring.leggTil(getOpplysning(123) as Opplysning<*>)

        ding.måAvklares(opplysninger).also { avklaringer ->
            avklaringer.size shouldBe 0
        }

        // Opplysningen endres tilbake til tilstand som krever avklaring
        val endretOpplysning = getOpplysning(321)
        regelkjøring.leggTil(endretOpplysning as Opplysning<*>)

        ding.måAvklares(opplysninger).also { avklaringer ->
            avklaringer.size shouldBe 1
            avklaringer.all { it.måAvklares() } shouldBe true
            avklaringer.all { it.kode == TestIkke123 } shouldBe true
            avklaringer.first().sistEndret.shouldBeAfter(endretOpplysning.opprettet)
        }
    }

    @Test
    fun `avklaringer av sykepenger i inntekt`() {
        val kontrollpunkter =
            listOf(
                // Om inntektene inneholder sykepenger, må det avklares om de er svangerskapsrelaterte
                Kontrollpunkt(SvangerskapsrelaterteSykepenger) { opplysninger ->
                    opplysninger.finnOpplysning(inntekterInneholderSykepenger).verdi
                },
            )

        val opplysninger = Opplysninger()
        val regelkjøring = Regelkjøring(1.mai(2024), opplysninger)
        regelkjøring.leggTil(Faktum<Boolean>(inntekterInneholderSykepenger, true) as Opplysning<*>)

        val ding = Avklaringer(kontrollpunkter)
        ding.måAvklares(opplysninger).also { avklaringer ->
            avklaringer.size shouldBe 1
            avklaringer.all { it.måAvklares() } shouldBe true
            avklaringer.all { it.kode == SvangerskapsrelaterteSykepenger } shouldBe true
        }

        // Saksbehandler kvittererer ut avklaringen fordi sykepengene ikke er svangerskapsrelaterte
        ding.avklaringer.first().kvittering()

        // Nå skal det ikke være avklaringer som må avklares
        ding.måAvklares(opplysninger).also { avklaringer ->
            avklaringer.size shouldBe 0
        }
    }

    @Test
    fun `avklaringer av beregningsregel for fastsatt vanlig arbeidstid`() {
        val regel1 = Opplysningstype.somBoolsk("6mnd")
        val regel2 = Opplysningstype.somBoolsk("12mnd")
        val regel3 = Opplysningstype.somBoolsk("36mnd")

        val kontrollpunkter =
            listOf(
                // Minst en beregningsregel må settes for fastsatt vanlig arbeidstid
                Kontrollpunkt(BeregningsregelForFVA) { opplysninger ->
                    listOf(
                        opplysninger.finnOpplysning(regel1),
                        opplysninger.finnOpplysning(regel2),
                        opplysninger.finnOpplysning(regel3),
                    ).count { it.verdi } != 1
                },
            )

        val opplysninger = Opplysninger()
        val regelkjøring = Regelkjøring(1.mai(2024), opplysninger)
        regelkjøring.leggTil(Faktum<Boolean>(regel1, false) as Opplysning<*>)
        regelkjøring.leggTil(Faktum<Boolean>(regel2, false) as Opplysning<*>)
        regelkjøring.leggTil(Faktum<Boolean>(regel3, false) as Opplysning<*>)

        val ding = Avklaringer(kontrollpunkter)
        ding.måAvklares(opplysninger).also { avklaringer ->
            avklaringer.size shouldBe 1
            avklaringer.all { it.måAvklares() } shouldBe true
            avklaringer.all { it.kode == BeregningsregelForFVA } shouldBe true
        }

        regelkjøring.leggTil(Faktum<Boolean>(regel1, true) as Opplysning<*>)

        // Denne avklaringen skal ikke kunne kvitteres ut, den krever endring
        shouldThrow<IllegalArgumentException> {
            ding.avklaringer.first().kvittering()
        }

        // Nå skal det ikke være avklaringer som må avklares
        ding.måAvklares(opplysninger).also { avklaringer ->
            avklaringer.size shouldBe 0
        }
    }
}
