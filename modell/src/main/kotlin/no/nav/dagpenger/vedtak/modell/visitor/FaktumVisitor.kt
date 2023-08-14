package no.nav.dagpenger.vedtak.modell.visitor

import no.nav.dagpenger.vedtak.modell.entitet.Beløp
import no.nav.dagpenger.vedtak.modell.entitet.Stønadsdager
import no.nav.dagpenger.vedtak.modell.entitet.Timer

interface FaktumVisitor {

    fun visitVanligArbeidstidPerDag(timer: Timer) {}

    fun visitDagsats(beløp: Beløp) {}

    fun visitAntallStønadsdager(dager: Stønadsdager) {}
}
