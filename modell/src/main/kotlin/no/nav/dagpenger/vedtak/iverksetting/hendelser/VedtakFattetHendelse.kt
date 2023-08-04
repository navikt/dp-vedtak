package no.nav.dagpenger.vedtak.iverksetting.hendelser

import no.nav.dagpenger.aktivitetslogg.Aktivitetslogg
import no.nav.dagpenger.vedtak.iverksetting.IverksettingsVedtak
import no.nav.dagpenger.vedtak.modell.hendelser.Hendelse
import java.util.UUID

class VedtakFattetHendelse(
    meldingsreferanseId: UUID,
    ident: String,
    val iverksettingsVedtak: IverksettingsVedtak,
    aktivitetslogg: Aktivitetslogg = Aktivitetslogg(),
) :
    Hendelse(meldingsreferanseId, ident, aktivitetslogg) {
    override fun kontekstMap(): Map<String, String> = emptyMap()
}
