package no.nav.dagpenger.hendelse

import no.nav.dagpenger.Rettighet
import no.nav.dagpenger.Vedtak.VedtaksFactory.Companion.avslag
import no.nav.dagpenger.Vedtak.VedtaksFactory.Companion.invilgelse

class OmgjøringHendelse(omgjøring: OmgjøringsVedtak) : Hendelse {

}

class OmgjøringsVedtak (opprinneligVedtaksId: String, nyRettighet : Rettighet) {}
