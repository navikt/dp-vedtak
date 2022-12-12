package no.nav.dagpenger.vedtak.modell

import no.nav.dagpenger.vedtak.modell.hendelser.NyRettighetHendelse

class VedtakHistorikk private constructor(private val vedtak: MutableList<Vedtak>) {

    constructor() : this(mutableListOf())

    fun leggTilVedtak(nyRettighetHendelse: NyRettighetHendelse) {
        vedtak.add(Vedtak())
    }

    fun beregn(aktivitetsTidslinjer: AktivitetsTidslinjer): BeregnetTidslinje {
        val rapporteringsPeriode = aktivitetsTidslinjer.rapporteringsPerioder.first()
        return BeregnetTidslinje(rapporteringsPeriode.dager.map { BeregnetDag(it.dato, beløp = 500) })
    }

    fun harVedtak() = vedtak.isNotEmpty()
}
