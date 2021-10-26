package no.nav.dagpenger

import no.nav.dagpenger.Vedtak.VedtaksFactory
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class VedtakFactoryTest {

    @Test
    fun `Kan lage vedtak av prosessresultathendelse`() {

        val vedtak = VedtaksFactory.invilgelse()
        assertTrue(vedtak.erAktiv())

    }

}
