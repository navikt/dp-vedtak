package no.nav.dagpenger.vedtak.modell

import no.nav.dagpenger.vedtak.modell.hendelser.NyRettighetHendelse
import no.nav.dagpenger.vedtak.modell.hendelser.RapporteringHendelse

class Person(id: PersonIdentifikator) {
    private val aktivitetsTidslinje = AktivitetsTidslinje()
    private val vedtakHistorikk = VedtakHistorikk()
    private val beregningHistorikk = BeregningHistorikk()

    fun håndter(nyRettighetHendelse: NyRettighetHendelse) {
        vedtakHistorikk.leggTilVedtak(nyRettighetHendelse)
    }

    fun håndter(rapporteringHendelse: RapporteringHendelse) {
        aktivitetsTidslinje.håndter(rapporteringHendelse)
        val beregnetTidslinje = vedtakHistorikk.beregn(aktivitetsTidslinje)
        beregningHistorikk.leggTilTidslinje(beregnetTidslinje)
    }

    fun harVedtak() = vedtakHistorikk.harVedtak()
    fun dagerTilBetaling() = beregningHistorikk.beregningTidslinjer.flatMap { it.beregnedeDager }
}
