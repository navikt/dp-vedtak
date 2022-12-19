package no.nav.dagpenger.vedtak.modell

import no.nav.dagpenger.vedtak.modell.hendelser.Ordinær
import no.nav.dagpenger.vedtak.modell.hendelser.RapporteringHendelse
import no.nav.dagpenger.vedtak.modell.visitor.PersonVisitor

class Person(private val id: PersonIdentifikator) {
    private val aktivitetsTidslinje = AktivitetsTidslinje()
    private val vedtakHistorikk = VedtakHistorikk()
    private val beregningHistorikk = BeregningHistorikk()

    fun håndter(ordinær: Ordinær) {
        vedtakHistorikk.leggTilVedtak(ordinær)
    }

    fun håndter(rapporteringHendelse: RapporteringHendelse) {
        aktivitetsTidslinje.håndter(rapporteringHendelse)
        val beregnetTidslinje = vedtakHistorikk.beregn(aktivitetsTidslinje)
        beregningHistorikk.leggTilTidslinje(beregnetTidslinje)
    }

    fun accept(visitor: PersonVisitor) {
        visitor.visitPerson(id)
        vedtakHistorikk.accept(visitor)
        beregningHistorikk.accept(visitor)
    }
}
