package no.nav.dagpenger.behandling.regel

import no.nav.dagpenger.behandling.Faktum
import no.nav.dagpenger.behandling.Opplysninger
import no.nav.dagpenger.behandling.Opplysningstype
import no.nav.dagpenger.behandling.Regelkjøring
import no.nav.dagpenger.behandling.Regelsett
import no.nav.dagpenger.behandling.mai
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SisteDagIMånedEllerLikTest {
    private val vilkår = Opplysningstype<Boolean>("Vilkår")
    private val a = Opplysningstype<Double>("A")
    private val b = Opplysningstype<Double>("B")

    private val opplysninger = Opplysninger()
    private val regelkjøring =
        Regelkjøring(
            1.mai,
            opplysninger,
            Regelsett("regelsett") {
                regel { vilkår.størreEnnEllerLik(a, b) }
            },
        )

    @Test
    fun `større enn`() {
        opplysninger.leggTil(Faktum(a, 2.0))
        opplysninger.leggTil(Faktum(b, 1.0))
        val utledet = opplysninger.finnOpplysning(vilkår)
        assertTrue(utledet.verdi)
    }

    @Test
    fun `ikke større enn`() {
        opplysninger.leggTil(Faktum(a, 2.0))
        opplysninger.leggTil(Faktum(b, 1.0))
        val utledet = opplysninger.finnOpplysning(vilkår)
        assertTrue(utledet.verdi)
    }
}
