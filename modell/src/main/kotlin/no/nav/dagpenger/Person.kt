package no.nav.dagpenger

import no.nav.dagpenger.Sak.Companion.finnEllerOpprettSak
import no.nav.dagpenger.Sak.Companion.harTilknyttetSøknad
import no.nav.dagpenger.Sak.Companion.hentEksisterendeSak
import no.nav.dagpenger.hendelse.SøknadOmNyRettighetHendelse


/* En person har oversikt over sine egne saker */
class Person {
    private val saker = mutableListOf<Sak>()

    fun søk(søknadshendelse: SøknadOmNyRettighetHendelse) {
        saker.finnEllerOpprettSak(this,søknadshendelse.søknadsReferanse)
    }

    fun harSak(søknadsReferanse: String): Boolean = saker.harTilknyttetSøknad(søknadsReferanse)

}