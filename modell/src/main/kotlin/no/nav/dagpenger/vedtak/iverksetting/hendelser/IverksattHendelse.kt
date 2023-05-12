package no.nav.dagpenger.vedtak.iverksetting.hendelser

import no.nav.dagpenger.vedtak.modell.Aktivitetslogg
import no.nav.dagpenger.vedtak.modell.hendelser.Hendelse
import java.util.UUID

open class IverksattHendelse(
    private val ident: String,
    private val iverksettingId: UUID,
    aktivitetslogg: Aktivitetslogg = Aktivitetslogg(),
) : Hendelse(ident, aktivitetslogg) {

    override fun kontekstMap(): Map<String, String> = mapOf("iverksettingId" to iverksettingId.toString())
}
