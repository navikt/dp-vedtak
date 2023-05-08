package no.nav.dagpenger.vedtak.iverksetting

import no.nav.dagpenger.vedtak.modell.Aktivitetslogg

internal class IverksettingInspektør(iverksetting: Iverksetting) : IverksettingVisitor {

    init {
        iverksetting.accept(this)
    }

    lateinit var innsendingLogg: Aktivitetslogg

    override fun postVisitAktivitetslogg(aktivitetslogg: Aktivitetslogg) {
        this.innsendingLogg = aktivitetslogg
    }
}
