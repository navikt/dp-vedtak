package no.nav.dagpenger

import no.nav.dagpenger.hendelse.SøknadOmNyRettighetHendelse

class Sak internal constructor(
    private val person: Person,
    private val søknadsreferanse: String
) {
    val vedtak = listOf<Vedtak>()

    companion object {
        fun List<Sak>.harTilknyttetSøknad(søknadsreferanse: String) =
            this.filter { it.harSøknad(søknadsreferanse) }.size == 1
        fun  List<Sak>.hentEksisterendeSak(søknadshendelse: SøknadOmNyRettighetHendelse) =
            this.first { it.harSøknad(søknadshendelse.søknadsReferanse) }
        fun List<Sak>.finnEllerOpprettSak(søknadshendelse: SøknadOmNyRettighetHendelse) =

    }

    fun harSøknad(søknadsreferanse: String) = this.søknadsreferanse == søknadsreferanse
}
