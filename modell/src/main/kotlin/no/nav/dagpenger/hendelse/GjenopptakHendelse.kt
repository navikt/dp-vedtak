package no.nav.dagpenger.hendelse

import no.nav.dagpenger.Vedtak.VedtaksFactory.Companion.avslag
import no.nav.dagpenger.Vedtak.VedtaksFactory.Companion.invilgelse

class GjenopptakHendelse(utfall: Boolean) : Hendelse {
    internal val vedtak = when (utfall) {
        true -> invilgelse()
        false -> avslag()
    }
}
