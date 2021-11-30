package no.nav.dagpenger.vedtak.modell.beregningsregler

import no.nav.dagpenger.vedtak.modell.Mengde
import no.nav.dagpenger.vedtak.modell.hendelse.BokføringsHendelse
import no.nav.dagpenger.vedtak.modell.konto.Konto

internal class SatsBeregningsregel(val sats: Double, konto: Konto) : Beregningsregel(konto) {
    override fun beregn(bokføringsHendelse: BokføringsHendelse): Mengde {
        TODO("Not yet implemented")
    }
}
