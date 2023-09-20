package no.nav.dagpenger.vedtak.modell.hendelser

import no.nav.dagpenger.aktivitetslogg.Aktivitetslogg
import no.nav.dagpenger.aktivitetslogg.AktivitetsloggHendelse
import no.nav.dagpenger.aktivitetslogg.IAktivitetslogg
import no.nav.dagpenger.aktivitetslogg.SpesifikkKontekst
import java.util.UUID

abstract class Hendelse(
    private val meldingsreferanseId: UUID,
    private val ident: String,
    internal val aktivitetslogg: Aktivitetslogg,
) : AktivitetsloggHendelse, IAktivitetslogg by aktivitetslogg {

    override fun ident() = ident

    override fun toSpesifikkKontekst(): SpesifikkKontekst {
        return SpesifikkKontekst(this.javaClass.simpleName, mapOf("ident" to ident) + kontekstMap())
    }

    fun toLogString(): String = aktivitetslogg.toString()

    override fun meldingsreferanseId() = meldingsreferanseId
    abstract fun kontekstMap(): Map<String, String>
}
