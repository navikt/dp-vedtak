package no.nav.dagpenger.vedtak.modell.konto

import no.nav.dagpenger.vedtak.modell.mengder.RatioMengde
import java.time.LocalDate

internal class Konto private constructor(
    private val posteringer: MutableList<Postering>
) {
    constructor() : this(mutableListOf())

    fun leggTilPostering(postering: Postering) {
        // sjekk at mengde er samme type (en konto kan bare spore enten tid, eller penger. Ikke begge)
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
