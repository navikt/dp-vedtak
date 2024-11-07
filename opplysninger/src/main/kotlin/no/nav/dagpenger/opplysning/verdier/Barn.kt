package no.nav.dagpenger.opplysning.verdier

import java.time.LocalDate

class BarnListe(barn: List<Barn>) : ComparableListe<Barn>(barn)

abstract class ComparableListe<T : Comparable<T>>(
    private val liste: List<T>,
) : Comparable<ComparableListe<T>>, List<T> by liste {
    override fun compareTo(other: ComparableListe<T>): Int = 0
}

data class Barn(
    val fødselsdato: LocalDate,
    val fornavnOgMellomnavn: String? = null,
    val etternavn: String? = null,
    val statsborgerskap: String? = null,
    val kvalifiserer: Boolean,
) : Comparable<Barn> {
    override fun compareTo(other: Barn): Int = this.fødselsdato.compareTo(other.fødselsdato)
}
