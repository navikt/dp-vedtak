package no.nav.dagpenger.vedtak.modell.beregningsregler

import no.nav.dagpenger.vedtak.modell.hendelse.BokføringsHendelse
import no.nav.dagpenger.vedtak.modell.konto.Konto
import no.nav.dagpenger.vedtak.modell.konto.Postering
import no.nav.dagpenger.vedtak.modell.tid.quantity.IntervallMengde

internal abstract class Beregningsregel(private val konto: Konto) {
    fun håndter(bokføringsHendelse: BokføringsHendelse) {
        lagPostering(bokføringsHendelse, beregn(bokføringsHendelse))
    }

    private fun lagPostering(bokføringsHendelse: BokføringsHendelse, mengde: IntervallMengde) {
        Postering(mengde, bokføringsHendelse.datoSett).also {
            konto.leggTilPostering(it)
            bokføringsHendelse.leggTilPostering(it)
        }
    }

    abstract fun beregn(bokføringsHendelse: BokføringsHendelse): IntervallMengde
}
