package no.nav.dagpenger.vedtak.modell

import no.nav.dagpenger.aktivitetslogg.Aktivitetskontekst
import no.nav.dagpenger.aktivitetslogg.SpesifikkKontekst

class PersonIdentifikator(private val ident: String) : Aktivitetskontekst {

    init {
        require(ident.matches(Regex("\\d{11}"))) { "personident må ha 11 siffer" }
    }

    companion object {
        fun String.tilPersonIdentfikator() = PersonIdentifikator(this)
    }

    fun identifikator() = ident

    override fun toSpesifikkKontekst() = SpesifikkKontekst("Person", mapOf("ident" to ident))

    override fun equals(other: Any?): Boolean = other is PersonIdentifikator && other.ident == this.ident

    override fun hashCode() = ident.hashCode()
}
