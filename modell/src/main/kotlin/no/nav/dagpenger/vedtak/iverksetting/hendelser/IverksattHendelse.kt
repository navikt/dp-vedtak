package no.nav.dagpenger.vedtak.iverksetting.hendelser

import no.nav.dagpenger.vedtak.modell.SpesifikkKontekst
import no.nav.dagpenger.vedtak.modell.hendelser.Hendelse
import java.util.UUID

open class IverksattHendelse(private val ident: String, private val iverksettingId: UUID) : Hendelse(ident) {
    override fun toSpesifikkKontekst() = SpesifikkKontekst(
        kontekstType = this.javaClass.simpleName,
        kontekstMap = mapOf("ident" to ident, "iverksettingId" to iverksettingId.toString()),
    )
}
