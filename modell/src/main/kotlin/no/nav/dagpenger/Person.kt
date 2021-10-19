package no.nav.dagpenger

import no.nav.dagpenger.Sak.Companion.harTilknyttetSøknad
import no.nav.dagpenger.hendelse.SøknadOmNyRettighetHendelse


/* En person har oversikt over sine egne saker */
class Person {
    private val saker = mutableListOf<Sak>()

    fun oppdaterSak() {

    }

    fun leggTilSak() {

    }

    fun søk(søknadshendelse: SøknadOmNyRettighetHendelse) {

        //saker.finnEllerOpprettSak(søknadshendelse.søknadsReferanse)

        if (saker.harTilknyttetSøknad(søknadsreferanse = søknadshendelse.søknadsReferanse)) {
            saker.add(Sak(this, søknadshendelse.søknadsReferanse))
        } else {
            saker.find { it.harSøknad(søknadshendelse.søknadsReferanse) }
        }
    }

    fun harSak(søknadsReferanse: String): Boolean = saker.harTilknyttetSøknad(søknadsReferanse)

}