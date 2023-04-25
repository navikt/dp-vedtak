package no.nav.dagpenger.vedtak.modell.entitet

import io.kotest.assertions.assertSoftly
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import no.nav.dagpenger.vedtak.hjelpere.februar
import no.nav.dagpenger.vedtak.hjelpere.januar
import no.nav.dagpenger.vedtak.hjelpere.mars
import org.junit.jupiter.api.Test

class PeriodeTest {

    @Test
    fun `konsistens test`() {
        shouldThrow<IllegalArgumentException> { Periode(1.februar, 1.januar) }
        val periode = Periode(1.februar, 1.mars)
        val datoer = 1.februar..1.mars
        periode.forEach { dato ->
            datoer.contains(dato) shouldBe true
        }
    }

    @Test
    fun `kan legge sammen perioder`() {
        val førstePeriode = Periode(1.februar, 10.februar)
        val andrePeriode = Periode(1.februar, 1.mars)
        val sammenstiltPeriode = førstePeriode + andrePeriode
        assertSoftly {
            sammenstiltPeriode.start shouldBe 1.februar
            sammenstiltPeriode.endInclusive shouldBe 1.mars
        }
    }
}
