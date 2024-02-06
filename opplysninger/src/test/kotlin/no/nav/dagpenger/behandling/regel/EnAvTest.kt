package no.nav.dagpenger.behandling.regel

import io.kotest.matchers.shouldBe
import no.nav.dagpenger.behandling.Faktum
import no.nav.dagpenger.behandling.Opplysninger
import no.nav.dagpenger.behandling.Opplysningstype
import no.nav.dagpenger.behandling.Regelkjøring
import no.nav.dagpenger.behandling.Regelsett
import no.nav.dagpenger.behandling.mai
import org.junit.jupiter.api.Test

internal class EnAvTest {
    private val opplysningB = Opplysningstype<Boolean>("B")
    private val opplysningC = Opplysningstype<Boolean>("C")
    private val produserer = Opplysningstype<Boolean>("A")
    private val opplysninger = Opplysninger()
    private val regelkjøring =
        Regelkjøring(
            1.mai,
            opplysninger,
            Regelsett("regelsett").also {
                it.enAv(
                    produserer,
                    opplysningB,
                    opplysningC,
                )
            },
        )
    private val regel =
        EnAv(Opplysningstype("A"), opplysningB, opplysningC)

    @Test
    fun `hvis en av opplysningene er sanne så er utledningen sann`() {
        opplysninger.leggTil(Faktum(opplysningB, false))
        opplysninger.leggTil(Faktum(opplysningC, true))
        val utledet = opplysninger.finnOpplysning(produserer)
        utledet.verdi shouldBe true
    }

    @Test
    fun `hvis ingen av opplysningene er sanne så er utledningen usann`() {
        opplysninger.leggTil(Faktum(opplysningB, false))
        opplysninger.leggTil(Faktum(opplysningC, false))
        val utledet = opplysninger.finnOpplysning(produserer)
        utledet.verdi shouldBe false
    }

    @Test
    fun `hvis begge opplysningene er sanne så er utledningen sann`() {
        opplysninger.leggTil(Faktum(opplysningB, true))
        opplysninger.leggTil(Faktum(opplysningC, true))
        val utledet = opplysninger.finnOpplysning(produserer)
        utledet.verdi shouldBe true
        utledet.verdi shouldBe true
    }
}
