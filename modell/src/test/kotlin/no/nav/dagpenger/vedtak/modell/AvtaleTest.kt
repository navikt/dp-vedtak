package no.nav.dagpenger.vedtak.modell

import no.nav.dagpenger.vedtak.modell.beregningsregler.StønadsperiodeBeregningsregel
import no.nav.dagpenger.vedtak.modell.helpers.februar
import no.nav.dagpenger.vedtak.modell.helpers.januar
import no.nav.dagpenger.vedtak.modell.hendelse.BokføringsHendelseType
import no.nav.dagpenger.vedtak.modell.konto.Konto
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class AvtaleTest {
    val person = Person()

    @Test
    fun `Kan legge til beregningsregler med fra og med dato`() {
        val beregningsregel = StønadsperiodeBeregningsregel(Konto())
        val avtale = Avtale().also {
            it.leggTilBeregningsregel(BokføringsHendelseType.Kvotebruk, beregningsregel, 15.januar)
        }

        assertEquals(beregningsregel, avtale.finnBeregningsregel(BokføringsHendelseType.Kvotebruk, 16.januar))
        val nyBeregningsregel = StønadsperiodeBeregningsregel(Konto())
        avtale.leggTilBeregningsregel(BokføringsHendelseType.Kvotebruk, nyBeregningsregel, 20.januar)

        assertEquals(beregningsregel, avtale.finnBeregningsregel(BokføringsHendelseType.Kvotebruk, 16.januar))
        assertEquals(nyBeregningsregel, avtale.finnBeregningsregel(BokføringsHendelseType.Kvotebruk, 20.januar))
        assertEquals(nyBeregningsregel, avtale.finnBeregningsregel(BokføringsHendelseType.Kvotebruk, 20.februar))

        assertThrows<IllegalArgumentException> {
            avtale.finnBeregningsregel(BokføringsHendelseType.Kvotebruk, 10.januar)
        }

        assertThrows<IllegalArgumentException> {
            assertEquals(null, avtale.finnBeregningsregel(BokføringsHendelseType.Meldekort, 16.januar))
        }
    }

    @Test
    fun `Det finnes ingen gjeldene avtale`() {
    }

    @Test
    fun `Det finnes kun en avtale, og avtalen er aktiv`() {
    }

    @Test
    fun `Det finnes flere avtaler, og en er aktiv`() {
    }

    @Test
    fun `Det finnes flere avtaler, ingen av avtalene er aktiv`() {
    }
}
