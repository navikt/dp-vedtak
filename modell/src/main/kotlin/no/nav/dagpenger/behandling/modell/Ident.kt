package no.nav.dagpenger.behandling.modell

import no.nav.dagpenger.behandling.modell.Ident.Companion.tilPersonIdentfikator

data class Ident(private val ident: String) {
    init {
        require(ident.matches(Regex("\\d{11}"))) { "personident må ha 11 siffer" }
    }

    companion object {
        fun String.tilPersonIdentfikator() = Ident(this)
    }

    fun identifikator() = ident

    fun alleIdentifikatorer() = listOf(ident)

    override fun toString(): String {
        return "Ident(${ident.substring(0, 6)}*****)"
    }
}