package no.nav.dagpenger.vedtak.modell.beregningsregler

import no.nav.dagpenger.vedtak.modell.hendelse.BokføringsHendelse
import no.nav.dagpenger.vedtak.modell.konto.Konto
import no.nav.dagpenger.vedtak.modell.mengder.IntervallMengde

internal class SatsBeregningsregel(val sats: Double, konto: Konto) : Beregningsregel(konto) {
    override fun beregn(bokføringsHendelse: BokføringsHendelse): IntervallMengde {
        TODO("Not yet implemented")
    }
}
