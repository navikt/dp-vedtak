package no.nav.dagpenger.vedtak.modell

import no.nav.dagpenger.aktivitetslogg.Aktivitetskontekst
import no.nav.dagpenger.aktivitetslogg.SpesifikkKontekst
import no.nav.dagpenger.vedtak.modell.hendelser.Hendelse

class Person(
    private val ident: PersonIdentifikator,
) : Aktivitetskontekst {
    companion object {
        val kontekstType: String = "Person"
    }

    fun ident() = ident

    override fun toSpesifikkKontekst(): SpesifikkKontekst = SpesifikkKontekst(kontekstType, mapOf("ident" to ident.identifikator()))

    private fun kontekst(hendelse: Hendelse) {
        hendelse.kontekst(this)
    }
}
