package no.nav.dagpenger.vedtak.iverksetting.hendelser

import no.nav.dagpenger.vedtak.iverksetting.IverksettingsVedtak
import no.nav.dagpenger.vedtak.modell.hendelser.Hendelse

class VedtakFattetHendelse(ident: String, val iverksettingsVedtak: IverksettingsVedtak) : Hendelse(ident)
