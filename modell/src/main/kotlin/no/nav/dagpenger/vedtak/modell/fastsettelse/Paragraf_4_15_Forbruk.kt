package no.nav.dagpenger.vedtak.modell.fastsettelse

import no.nav.dagpenger.vedtak.modell.Aktivitetskontekst
import no.nav.dagpenger.vedtak.modell.SpesifikkKontekst
import no.nav.dagpenger.vedtak.modell.hendelser.Rapporteringshendelse
import no.nav.dagpenger.vedtak.modell.mengde.Enhet.Companion.arbeidsdager
import no.nav.dagpenger.vedtak.modell.mengde.Tid
import no.nav.dagpenger.vedtak.modell.rapportering.Arbeidsdag
import no.nav.dagpenger.vedtak.modell.rapportering.Dag
import no.nav.dagpenger.vedtak.modell.visitor.FastsettelseVisitor

internal class Paragraf_4_15_Forbruk() : Aktivitetskontekst {

    private var forbruk: Tid = 0.arbeidsdager

    fun accept(visitor: FastsettelseVisitor) {
        visitor.visitForbruk(forbruk)
    }

    fun håndter(rapporteringsHendelse: Rapporteringshendelse, tellendeDager: List<Dag>) {
        rapporteringsHendelse.kontekst(this)
        forbruk = tellendeDager.filterIsInstance<Arbeidsdag>().size.arbeidsdager
    }

    override fun toSpesifikkKontekst(): SpesifikkKontekst =
        SpesifikkKontekst(this.javaClass.simpleName)
}
