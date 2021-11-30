package no.nav.dagpenger.vedtak.modell.beregningsregler

import no.nav.dagpenger.vedtak.modell.Mengde
import no.nav.dagpenger.vedtak.modell.hendelse.BokføringsHendelse
import no.nav.dagpenger.vedtak.modell.konto.Konto
import no.nav.dagpenger.vedtak.modell.konto.Postering

internal abstract class Beregningsregel(private val konto: Konto) {
    fun håndter(bokføringsHendelse: BokføringsHendelse) {
        lagPostering(bokføringsHendelse, beregn(bokføringsHendelse))
    }

    private fun lagPostering(bokføringsHendelse: BokføringsHendelse, mengde: Mengde) {
        Postering(mengde, bokføringsHendelse.datoSett).also {
            konto.leggTilPostering(it)
            bokføringsHendelse.leggTilPostering(it)
        }
    }

    abstract fun beregn(bokføringsHendelse: BokføringsHendelse): Mengde
}
