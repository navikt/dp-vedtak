package no.nav.dagpenger.vedtak.iverksetting.hendelser

import no.nav.dagpenger.aktivitetslogg.Aktivitetslogg
import no.nav.dagpenger.vedtak.iverksetting.IverksettingsVedtak
import no.nav.dagpenger.vedtak.modell.hendelser.Hendelse

class VedtakFattetHendelse(ident: String, val iverksettingsVedtak: IverksettingsVedtak, aktivitetslogg: Aktivitetslogg = Aktivitetslogg()) :
    Hendelse(ident, aktivitetslogg) {
    override fun kontekstMap(): Map<String, String> = emptyMap()
}
