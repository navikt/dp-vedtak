package no.nav.dagpenger.opplysning.verdier

import java.time.LocalDate

data class Barn(
    val ident: String,
    val navn: String,
    val fdato: LocalDate,
    val kvalifisertTil: LocalDate?,
)

data class AlleBarn(
    val barn: List<Barn>,
) : Comparable<AlleBarn> {
    override fun compareTo(other: AlleBarn): Int = barn.size.compareTo(other.barn.size)
}
