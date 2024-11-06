package no.nav.dagpenger.opplysning.verdier

import java.time.LocalDate

data class Barn(
    val fødselsdato: LocalDate,
    val fornavnOgMellomnavn: String? = null,
    val etternavn: String? = null,
    val statsborgerskap: String? = null,
    val kvalifiserer: Boolean,
) : Comparable<Barn> {
    override fun compareTo(other: Barn): Int = this.fødselsdato.compareTo(other.fødselsdato)
}
