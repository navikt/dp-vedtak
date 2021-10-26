package no.nav.dagpenger

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class VedtakFactoryTest {

    @Test
    fun `Kan lage vedtak av prosessresultathendelse`(){

        val forberedtVedtak = Vedtak.forberedVedtak(utfall = true)

        assertTrue(forberedtVedtak.lag() is Vedtak)

    }

}
