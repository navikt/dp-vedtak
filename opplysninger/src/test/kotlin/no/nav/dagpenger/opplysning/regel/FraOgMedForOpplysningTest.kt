package no.nav.dagpenger.opplysning.regel

import io.kotest.matchers.shouldBe
import no.nav.dagpenger.opplysning.Faktum
import no.nav.dagpenger.opplysning.Gyldighetsperiode
import no.nav.dagpenger.opplysning.Opplysninger
import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.opplysning.Regelkjøring
import no.nav.dagpenger.opplysning.Regelsett
import no.nav.dagpenger.opplysning.januar
import org.junit.jupiter.api.Test
import java.time.LocalDate

class FraOgMedForOpplysningTest {
    private val vilkårType1 = Opplysningstype.somBoolsk("Vilkår 1")
    private val vilkårType2 = Opplysningstype.somBoolsk("Vilkår 2")
    private val vilkårType3 = Opplysningstype.somBoolsk("Vilkår 3")
    private val harKravPåDagpenger = Opplysningstype.somBoolsk("Alle vilkår er oppfylt")

    private val virkningstidspunktRegel =
        Regelsett("Virkningtidspunkt") {
            regel(harKravPåDagpenger) { alle(vilkårType1, vilkårType2, vilkårType3) }
        }

    @Test
    fun `finner første mulige dato hvor vilkår er oppfylt samtidig`() {
        val alleVilkår =
            listOf(
                Faktum(vilkårType1, true, Gyldighetsperiode(fom = 1.januar, tom = 5.januar)),
                Faktum(vilkårType1, true, Gyldighetsperiode(fom = 10.januar)),
                Faktum(vilkårType2, true, Gyldighetsperiode(fom = 10.januar)),
                Faktum(vilkårType3, true, Gyldighetsperiode(fom = 15.januar)),
            )

        val opplysninger = Opplysninger()
        val regelverksdato = 10.januar

        alleVilkår.forEach { vilkår ->
            opplysninger.leggTil(vilkår).also {
                val regelkjøring = Regelkjøring(regelverksdato, opplysninger, virkningstidspunktRegel)
                regelkjøring.evaluer()
            }
        }

        opplysninger.har(harKravPåDagpenger) shouldBe true
        opplysninger.finnOpplysning(harKravPåDagpenger).gyldighetsperiode.fom shouldBe 15.januar
    }

    @Test
    fun `finner prøvingsdato når ikke alle vilkår er oppfylt samtidig`() {
        val alleVilkår =
            listOf(
                Faktum(vilkårType1, true, Gyldighetsperiode(fom = 1.januar)),
                Faktum(vilkårType2, true, Gyldighetsperiode(fom = 1.januar, tom = 3.januar)),
                Faktum(vilkårType3, true, Gyldighetsperiode(fom = 5.januar)),
            )

        val opplysninger = Opplysninger()
        val regelverksdato = 2.januar

        alleVilkår.forEach { vilkår ->
            opplysninger.leggTil(vilkår).also {
                val regelkjøring = Regelkjøring(regelverksdato, opplysninger, virkningstidspunktRegel)
                regelkjøring.evaluer()
            }
        }

        opplysninger.har(harKravPåDagpenger) shouldBe false
    }

    @Test
    fun `tonjetest`() {
        val alleVilkår =
            listOf(
                { Faktum(vilkårType1, true, Gyldighetsperiode(fom = 10.januar, tom = 13.januar)) },
                { Faktum(vilkårType2, true, Gyldighetsperiode(fom = 10.januar, tom = 13.januar)) },
                // Faktum(vilkårType2, true, Gyldighetsperiode(fom = 15.januar, tom = 16.januar)),
                { Faktum(vilkårType3, true, Gyldighetsperiode(fom = 5.januar, tom = 13.januar)) },
                { Faktum(vilkårType3, true, Gyldighetsperiode(fom = 15.januar)) },
                { Faktum(vilkårType2, true, Gyldighetsperiode(fom = 15.januar)) },
                { Faktum(vilkårType1, true, Gyldighetsperiode(fom = 15.januar)) },
            )

        val regelverksdato = 10.januar

        val opplysninger = Opplysninger()

        alleVilkår.forEach { fabrikk ->
            val vilkår = fabrikk()
            opplysninger.leggTil(vilkår).also {
                val regelkjøring = Regelkjøring(regelverksdato, opplysninger, virkningstidspunktRegel)
                regelkjøring.evaluer()
            }
        }

        opplysninger.forDato(10.januar).apply {
            har(harKravPåDagpenger) shouldBe true
            finnOpplysning(harKravPåDagpenger).gyldighetsperiode.fom shouldBe 10.januar
            finnOpplysning(harKravPåDagpenger).gyldighetsperiode.tom shouldBe 13.januar
        }

        opplysninger.forDato(15.januar).apply {
            har(harKravPåDagpenger) shouldBe true
            finnOpplysning(harKravPåDagpenger).gyldighetsperiode.fom shouldBe 15.januar
            finnOpplysning(harKravPåDagpenger).gyldighetsperiode.tom shouldBe LocalDate.MAX
        }
    }
}
