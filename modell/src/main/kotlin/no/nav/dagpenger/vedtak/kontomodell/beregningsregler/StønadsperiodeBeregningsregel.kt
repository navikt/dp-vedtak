package no.nav.dagpenger.vedtak.kontomodell.beregningsregler

import no.nav.dagpenger.vedtak.kontomodell.hendelse.BokføringsHendelse
import no.nav.dagpenger.vedtak.kontomodell.hendelse.Kvotebruk
import no.nav.dagpenger.vedtak.kontomodell.konto.Konto
import no.nav.dagpenger.vedtak.kontomodell.mengder.Tid

internal class StønadsperiodeBeregningsregel(konto: Konto) : Beregningsregel(konto) {
    override fun beregn(bokføringsHendelse: BokføringsHendelse): Tid {
        require(bokføringsHendelse is Kvotebruk)
        return bokføringsHendelse.mengde
    }
}
