package no.nav.dagpenger.vedtak.modell.vedtak.fakta

import no.nav.dagpenger.vedtak.modell.entitet.Beløp
import no.nav.dagpenger.vedtak.modell.entitet.Stønadsdager
import no.nav.dagpenger.vedtak.modell.entitet.Timer
import no.nav.dagpenger.vedtak.modell.visitor.FaktumVisitor

interface Faktum<T> {

    fun accept(faktumVisitor: FaktumVisitor)
}

@JvmInline
value class VanligArbeidstidPerDag(val timer: Timer) : Faktum<Timer> {
    override fun accept(faktumVisitor: FaktumVisitor) {
        faktumVisitor.visitVanligArbeidstidPerDag(timer)
    }
}

@JvmInline
value class Grunnlag(val beløp: Beløp) : Faktum<Beløp> {
    override fun accept(faktumVisitor: FaktumVisitor) {
        faktumVisitor.visitGrunnlag(beløp)
    }
}

@JvmInline
value class Dagsats(val beløp: Beløp) : Faktum<Beløp> {
    override fun accept(faktumVisitor: FaktumVisitor) {
        faktumVisitor.visitDagsats(beløp)
    }
}

@JvmInline
value class AntallStønadsdager(val dager: Stønadsdager) : Faktum<Stønadsdager> {
    override fun accept(faktumVisitor: FaktumVisitor) {
        faktumVisitor.visitAntallStønadsdager(dager)
    }
}

@JvmInline
value class Egenandel(val beløp: Beløp) : Faktum<Beløp> {
    override fun accept(faktumVisitor: FaktumVisitor) {
        faktumVisitor.visitEgenandel(beløp)
    }
}
