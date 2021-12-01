package no.nav.dagpenger.vedtak.modell

import no.nav.dagpenger.vedtak.modell.hendelse.ArenaKvoteForbruk
import no.nav.dagpenger.vedtak.modell.hendelse.AvslagHendelse
import no.nav.dagpenger.vedtak.modell.hendelse.BarnetilleggSkalAvslåsHendelse
import no.nav.dagpenger.vedtak.modell.hendelse.InnvilgetProsessresultatHendelse
import no.nav.dagpenger.vedtak.modell.hendelse.StansHendelse
import no.nav.dagpenger.vedtak.modell.mengder.Enhet.Companion.arbeidsdager
import no.nav.dagpenger.vedtak.modell.mengder.Enhet.Companion.arbeidsuker
import no.nav.dagpenger.vedtak.modell.mengder.Tid
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.util.UUID

internal class PersonTest {
    private lateinit var person: Person
    private lateinit var observatør: TestObservatør
    private val sisteVedtak get() = observatør.vedtakFattet.toList().last().second

    @BeforeEach
    fun setup() {
        person = Person()
        observatør = TestObservatør().also { person.addObserver(it) }
    }

    @Test
    fun `Kan innvilge for person som ikke har hatt dagpenger før`() {
        innvilgVedtakOgAvtale(sats = 500.0)

        assertEquals(1, observatør.avtaler.size)
        assertEquals(1, observatør.vedtakFattet.size)
        assertEquals(500.0, sisteVedtak.sats?.antall)
    }

    @Test
    fun `Kan innvilge og forbruke kvote`() {
        innvilgVedtakOgAvtale(periode = 52.arbeidsuker)

        assertEquals(52.arbeidsuker, person.gjeldendeAvtale()?.balanse("Stønadsperiodekonto"), "Avtalen skal settes opp med 52 uker som stønadsperiode")
        person.håndter(ArenaKvoteForbruk((-10).arbeidsdager))
        assertEquals(50.arbeidsuker, person.gjeldendeAvtale()?.balanse("Stønadsperiodekonto"), "10 arbeidsdager skal forbruke 2 uker (a 5 dager hver)")
        assertEquals(250.arbeidsdager, person.gjeldendeAvtale()?.balanse("Stønadsperiodekonto"), "50 arbeidsuker skal tilsvare 250 arbeidsdager")
    }

    @Test
    fun `Avslag på rettighet for person som ikke har hatt dagpenger før`() {
        person.håndter(AvslagHendelse())

        assertEquals(1, observatør.vedtakFattet.size)
        assertEquals(null, sisteVedtak.sats?.antall)
    }

    @Test
    fun `Stans for person som har rettighet`() {
        innvilgVedtakOgAvtale()
        person.håndter(StansHendelse())

        assertEquals(1, observatør.avtaler.size)
        assertEquals(2, observatør.vedtakFattet.size)
    }

    @Test
    @Disabled("Vi vet ikke nok enda hvordan dette skal være")
    fun `Nytt barn fører til økt sats på gjeldende rettighet`() {
        innvilgVedtakOgAvtale()
        assertEquals(1, observatør.avtaler.size)
        assertEquals(1, observatør.vedtakFattet.size)
        // person.håndter(InnvilgetProsessresultatHendelse(sats = 1000.0))
        assertEquals(500.0, sisteVedtak.sats?.antall)
        assertEquals(2, observatør.vedtakFattet.size)
    }

    @Test
    @Disabled("Vi vet ikke nok enda hvordan dette skal være")
    fun `Nytt barn fører ikke til økt sats på gjeldende avtale, men vedtaket blir lagt til i historikk`() {
        innvilgVedtakOgAvtale(sats = 500.0)
        assertEquals(1, observatør.avtaler.size)
        assertEquals(1, observatør.vedtakFattet.size)

        person.håndter(BarnetilleggSkalAvslåsHendelse())
        // assertEquals(500.0, person.avtaler.gjeldende()?.sats())
        assertEquals(2, observatør.vedtakFattet.size)
        assertEquals(1, observatør.avtaler.size)
    }

    @Test
    fun `Innsending av meldekort uten avtale fører til hvafornoe`() {
        // TODO: Vil innsending av meldekort uten en avtale føre til en avtale?
    }

    private fun innvilgVedtakOgAvtale(sats: Double = 500.0, periode: Tid = 52.arbeidsuker) =
        person.håndter(InnvilgetProsessresultatHendelse(sats, periode))
}

class TestObservatør : PersonObserver {
    internal var vedtakFattet = mutableMapOf<UUID, PersonObserver.VedtakFattetEvent>()
    internal var avtaler = mutableSetOf<UUID>()

    override fun vedtakFattet(hendelse: PersonObserver.VedtakFattetEvent) {
        vedtakFattet[hendelse.vedtakId] = hendelse

        if (hendelse.avtaleId !== null) avtaler.add(hendelse.avtaleId!!)
    }
}
