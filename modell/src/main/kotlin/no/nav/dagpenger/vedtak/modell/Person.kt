package no.nav.dagpenger.vedtak.modell

import no.nav.dagpenger.vedtak.modell.hendelser.NyRettighetHendelse
import no.nav.dagpenger.vedtak.modell.hendelser.RapporteringHendelse
import no.nav.dagpenger.vedtak.modell.visitor.PersonVisitor

class Person(private val id: PersonIdentifikator) {
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

    fun dagerTilBetaling() = beregningHistorikk.beregningTidslinjer.flatMap { it.beregnedeDager }
    fun accept(visitor: PersonVisitor) {
        visitor.visitPerson(id)
        vedtakHistorikk.accept(visitor)
    }
}
