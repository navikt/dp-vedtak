package no.nav.dagpenger.vedtak.modell.beregningsregler

import no.nav.dagpenger.vedtak.modell.Mengde
import no.nav.dagpenger.vedtak.modell.hendelse.BokføringsHendelse
import no.nav.dagpenger.vedtak.modell.hendelse.Kvotebruk
import no.nav.dagpenger.vedtak.modell.konto.Konto

internal class StønadsperiodeBeregningsregel(konto: Konto) : Beregningsregel(konto) {
    override fun beregn(bokføringsHendelse: BokføringsHendelse): Mengde {
        require(bokføringsHendelse is Kvotebruk)
        return bokføringsHendelse.mengde
    }
}
