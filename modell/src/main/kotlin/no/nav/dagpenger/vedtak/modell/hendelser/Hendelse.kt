package no.nav.dagpenger.vedtak.modell.hendelser

import no.nav.dagpenger.aktivitetslogg.Aktivitetskontekst
import no.nav.dagpenger.aktivitetslogg.Aktivitetslogg
import no.nav.dagpenger.aktivitetslogg.IAktivitetslogg
import no.nav.dagpenger.aktivitetslogg.SpesifikkKontekst

abstract class Hendelse(
    private val ident: String,
    internal val aktivitetslogg: Aktivitetslogg,
) : Aktivitetskontekst, IAktivitetslogg by aktivitetslogg {

    fun ident() = ident

    override fun toSpesifikkKontekst(): SpesifikkKontekst {
        return SpesifikkKontekst(this.javaClass.simpleName, mapOf("ident" to ident) + kontekstMap())
    }

    fun toLogString(): String = aktivitetslogg.toString()

    abstract fun kontekstMap(): Map<String, String>
}
