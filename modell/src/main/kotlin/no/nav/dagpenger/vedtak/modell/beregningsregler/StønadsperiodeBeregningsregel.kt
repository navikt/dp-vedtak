package no.nav.dagpenger.vedtak.modell.beregningsregler

import no.nav.dagpenger.vedtak.modell.hendelse.BokføringsHendelse
import no.nav.dagpenger.vedtak.modell.hendelse.Kvotebruk
import no.nav.dagpenger.vedtak.modell.konto.Konto
import no.nav.dagpenger.vedtak.modell.mengder.Tid

internal class StønadsperiodeBeregningsregel(konto: Konto) : Beregningsregel(konto) {
    override fun beregn(bokføringsHendelse: BokføringsHendelse): Tid {
        require(bokføringsHendelse is Kvotebruk)
        return bokføringsHendelse.mengde
    }
}
