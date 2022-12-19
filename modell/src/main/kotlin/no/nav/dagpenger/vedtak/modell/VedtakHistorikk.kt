package no.nav.dagpenger.vedtak.modell

import no.nav.dagpenger.vedtak.modell.hendelser.Ordinær
import no.nav.dagpenger.vedtak.modell.visitor.VedtakHistorikkVisitor

class VedtakHistorikk private constructor(private val vedtak: MutableList<Vedtak>) {

    constructor() : this(mutableListOf())

    fun leggTilVedtak(ordinær: Ordinær) {
        vedtak.add(
            Vedtak(
                virkningsdato = ordinær.virkningsdato,
                beslutningstidspunkt = ordinær.beslutningstidspunkt,
                dagsats = ordinær.dagsats
            )
        )
    }

    fun beregn(aktivitetsTidslinje: AktivitetsTidslinje): BeregnetTidslinje {
        val rapporteringsPeriode = aktivitetsTidslinje.rapporteringsPerioder.first()
        return BeregnetTidslinje(rapporteringsPeriode.dager.map { BeregnetDag(it.dato, beløp = 500) })
    }

    fun accept(visitor: VedtakHistorikkVisitor) {
        visitor.preVisitVedtakHistorikk()
        vedtak.forEach { it.accept(visitor) }
        visitor.postVisitVedtakHistorikk()
    }
}
