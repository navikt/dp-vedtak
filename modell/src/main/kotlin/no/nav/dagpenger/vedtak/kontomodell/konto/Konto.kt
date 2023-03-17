package no.nav.dagpenger.vedtak.kontomodell.konto

import no.nav.dagpenger.vedtak.kontomodell.Avtale
import no.nav.dagpenger.vedtak.kontomodell.beregningsregler.StønadsperiodeBeregningsregel
import no.nav.dagpenger.vedtak.kontomodell.hendelse.BokføringsHendelseType
import no.nav.dagpenger.vedtak.kontomodell.mengder.RatioMengde
import java.time.LocalDate

internal class Konto private constructor(
    private val posteringer: MutableList<Postering>,
) {
    constructor() : this(mutableListOf())

    companion object {
        fun forStønadsperiode(avtale: Avtale, fraOgMed: LocalDate) = Konto().also {
            avtale.leggTilBeregningsregel(
                BokføringsHendelseType.Kvotebruk,
                StønadsperiodeBeregningsregel(it),
                fraOgMed,
            )
        }
    }

    fun leggTilPostering(postering: Postering) {
        if (posteringer.isNotEmpty() && !postering.mengde.erKompatibel(posteringer)) { throw IllegalArgumentException("Inkompatibel enhet for postering") }
        posteringer.add(postering)
    }

    private fun balanse(period: ClosedRange<LocalDate>) = posteringer.filter { it.dato in period }
        .map { it.mengde }
        .reduceOrNull { acc, ratioMengde -> acc + ratioMengde }

    fun balanse(fraOgMed: LocalDate, tilOgMed: LocalDate): RatioMengde? {
        require(fraOgMed.isBefore(tilOgMed)) { "Fra og med må være tidligere enn til og med" }
        return balanse(fraOgMed..tilOgMed)
    }

    fun balanse(tilOgMed: LocalDate) = balanse(LocalDate.MIN, tilOgMed)

    fun balanse() = balanse(LocalDate.now())
}
