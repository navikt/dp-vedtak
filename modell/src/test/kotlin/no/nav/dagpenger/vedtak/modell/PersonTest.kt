package no.nav.dagpenger.vedtak.modell

import no.nav.dagpenger.vedtak.modell.hendelse.ArenaKvoteForbruk
import no.nav.dagpenger.vedtak.modell.hendelse.AvslagHendelse
import no.nav.dagpenger.vedtak.modell.hendelse.BarnetilleggSkalAvslåsHendelse
import no.nav.dagpenger.vedtak.modell.hendelse.InnvilgetProsessresultatHendelse
import no.nav.dagpenger.vedtak.modell.hendelse.StansHendelse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class PersonTest {
    private val person = Person()

    @Test
    fun `Kan innvilge og forbruke kvote`() {
        innvilgVedtakOgAvtale(sats = 500.0)
        assertEquals(1, person.avtaler.size)
        assertEquals(1, person.vedtak.size)

        assertEquals(52, person.gjeldendeAvtale().balanse("Stønadsperiodekonto"))
        person.håndter(ArenaKvoteForbruk(-10))
        assertEquals(42, person.gjeldendeAvtale().balanse("Stønadsperiodekonto"))
    }

    @Test
    fun `Innvilgelse av rettighet for person som ikke har hatt dagpenger før`() {
        innvilgVedtakOgAvtale(sats = 500.0)
        assertEquals(1, person.avtaler.size)
        assertEquals(1, person.vedtak.size)
    }

    @Test
    fun `Avslag på rettighet for person som ikke har hatt dagpenger før`() {
        person.håndter(AvslagHendelse())

        assertEquals(1, person.vedtak.size)
        assertEquals(0, person.avtaler.size)
    }

    @Test
    fun `Stans for person som har rettighet`() {
        innvilgVedtakOgAvtale()
        person.håndter(StansHendelse())

        assertEquals(2, person.vedtak.size)
        assertEquals(1, person.avtaler.size)
    }

    @Test
    fun `Nytt barn fører til økt sats på gjeldende rettighet`() {
        innvilgVedtakOgAvtale()
        assertEquals(1, person.vedtak.size)
        assertEquals(1, person.avtaler.size)
        // person.håndter(BarnetilleggSkalInnvilgesHendelse(sats = 1000.0))
        // assertEquals(1000.0, person.avtaler.gjeldende()?.sats())
        // assertEquals(2, person.vedtak.size)
    }

    @Test
    fun `Nytt barn fører ikke til økt sats på gjeldende avtale, men vedtaket blir lagt til i historikk`() {
        innvilgVedtakOgAvtale(sats = 500.0)
        assertEquals(1, person.vedtak.size)
        assertEquals(1, person.avtaler.size)

        person.håndter(BarnetilleggSkalAvslåsHendelse())
        // assertEquals(500.0, person.avtaler.gjeldende()?.sats())
        assertEquals(2, person.vedtak.size)
        assertEquals(1, person.avtaler.size)
    }

    @Test
    fun `Innsending av meldekort fører til oppdatering av dagpengeperiode på avtalen men ikke nytt vedtak`() {
        innvilgVedtakOgAvtale()
        // person.håndter(InnsendtMeldekortHendelse())
    }

    @Test
    fun `Innsending av meldekort uten avtale fører til hvafornoe?`() {
        // TODO: Vil innsending av meldekort uten en avtale føre til en avtale?
    }

    @Test
    fun `Kvotebruk fører til redusering i periode`() {
    }

    private fun innvilgVedtakOgAvtale(sats: Double = 500.0) =
        person.håndter(InnvilgetProsessresultatHendelse(sats = sats, periode = 52))
}
