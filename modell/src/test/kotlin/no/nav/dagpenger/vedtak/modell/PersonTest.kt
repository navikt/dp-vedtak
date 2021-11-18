package no.nav.dagpenger.vedtak.modell

import no.nav.dagpenger.vedtak.modell.hendelse.AvslagHendelse
import no.nav.dagpenger.vedtak.modell.hendelse.BarnetilleggSkalAvslåsHendelse
import no.nav.dagpenger.vedtak.modell.hendelse.BarnetilleggSkalInnvilgesHendelse
import no.nav.dagpenger.vedtak.modell.hendelse.InnsendtMeldekortHendelse
import no.nav.dagpenger.vedtak.modell.hendelse.InnvilgetProsessresultatHendelse
import no.nav.dagpenger.vedtak.modell.hendelse.StansHendelse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class PersonTest {
    val person = Person()

    @Test
    fun `Person har ikke hatt tidligere, og får melding om ny rettighet`() {
        innvilgVedtakOgAvtale()
        assertEquals(1, person.avtaler.size)
        assertEquals(1, person.vedtak.size)
        assertEquals(500.0, person.avtaler.last().sats())
    }

    @Test
    fun `Person har ikke hatt tidligere, og får melding om avslag`() {
        person.håndter(AvslagHendelse())

        assertEquals(1, person.vedtak.size)
        assertEquals(0, person.avtaler.size)
    }

    @Test
    fun `Person har rettighet fra før og skal stanse denne`() {
        innvilgVedtakOgAvtale()
        person.håndter(StansHendelse())

        assertEquals(2, person.vedtak.size)
        assertEquals(1, person.avtaler.size)
    }

    @Test
    fun `Nytt barn fører til økt sats på gjeldende avtale`() {
        innvilgVedtakOgAvtale()
        assertEquals(1, person.vedtak.size)
        assertEquals(1, person.avtaler.size)

        person.håndter(BarnetilleggSkalInnvilgesHendelse(sats = 1000.0))
        assertEquals(1000.0, person.avtaler.last().sats())
        assertEquals(2, person.vedtak.size)
    }

    @Test
    fun `Nytt barn fører ikke til økt sats på gjeldende avtale, men vedtaket blir lagt til i historikk`() {
        innvilgVedtakOgAvtale(sats = 500.0)
        assertEquals(1, person.vedtak.size)
        assertEquals(1, person.avtaler.size)

        person.håndter(BarnetilleggSkalAvslåsHendelse())
        assertEquals(500.0, person.avtaler.last().sats())
        assertEquals(2, person.vedtak.size)
        assertEquals(1, person.avtaler.size)
    }

    @Test
    fun `Innsending av meldekort fører til oppdatering av dagpenge periode på avtalen men ikke nytt vedtak`() {
        innvilgVedtakOgAvtale()
        person.håndter(InnsendtMeldekortHendelse())
    }

    @Test
    fun `Innsending av meldekort uten avtale fører til hvafornoe?`() {
        // TODO: Vil innsending av meldekort uten en avtale føre til en avtale?
    }

    private fun innvilgVedtakOgAvtale(sats: Double = 500.0) = person.håndter(InnvilgetProsessresultatHendelse(sats = sats))
}
