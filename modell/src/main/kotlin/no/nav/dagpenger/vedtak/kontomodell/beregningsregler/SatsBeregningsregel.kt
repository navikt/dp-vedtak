package no.nav.dagpenger.vedtak.kontomodell.beregningsregler

import no.nav.dagpenger.vedtak.kontomodell.hendelse.BokføringsHendelse
import no.nav.dagpenger.vedtak.kontomodell.konto.Konto
import no.nav.dagpenger.vedtak.kontomodell.mengder.RatioMengde

internal class SatsBeregningsregel(val sats: Double, konto: Konto) : Beregningsregel(konto) {
    override fun beregn(bokføringsHendelse: BokføringsHendelse): RatioMengde {
        TODO("Not yet implemented")
    }
}
