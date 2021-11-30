package no.nav.dagpenger.vedtak.modell.konto

import no.nav.dagpenger.vedtak.modell.tid.quantity.Enhet.Companion.arbeidsdager
import java.time.LocalDate

internal class Konto(private val posteringer: MutableList<Postering>) {
    constructor() : this(mutableListOf())

    fun leggTilPostering(postering: Postering) {
        // sjekk at mengde er tid
        posteringer.add(postering)
    }

    fun balanse(period: ClosedRange<LocalDate>) =
        posteringer.filter { it.dato in period }.fold(0.arbeidsdager) { acc, postering -> acc + postering.mengde }

    fun balanse(dato: LocalDate) = balanse(LocalDate.MIN..dato)

    fun balanse() = balanse(LocalDate.now())
}
