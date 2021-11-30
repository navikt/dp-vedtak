package no.nav.dagpenger.vedtak.modell.konto

import java.time.LocalDate

internal class Konto(private val posteringer: MutableList<Postering>) {

    constructor() : this(mutableListOf())

    fun leggTilPostering(postering: Postering) {
        // sjekk at mengde er tid
        posteringer.add(postering)
    }

    fun balanse(period: ClosedRange<LocalDate>) = posteringer.filter { it.dato in period }.sumOf { it.mengde }

    fun balanse(dato: LocalDate) = balanse(LocalDate.MIN..dato)

    fun balanse() = balanse(LocalDate.now())
}
