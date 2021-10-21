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

        fun List<Sak>.hentEksisterendeSak(søknadshendelse: SøknadOmNyRettighetHendelse) =
            this.first { it.harSøknad(søknadshendelse.søknadsReferanse) }

        fun MutableList<Sak>.finnEllerOpprettSak(person: Person, søknadshendelse: SøknadOmNyRettighetHendelse) {
            if (this.harTilknyttetSøknad(søknadsreferanse = søknadshendelse.søknadsReferanse)) {
                this.add(Sak(person, søknadshendelse.søknadsReferanse))
            } else {
                this.hentEksisterendeSak(søknadshendelse).oppdater(søknadshendelse)

            }
        }
    }

    private fun oppdater(søknadshendelse: SøknadOmNyRettighetHendelse) {
        TODO("Not yet implemented")
    }

    fun harSøknad(søknadsreferanse: String) = this.søknadsreferanse == søknadsreferanse
}

/*
* Forstår søknadsinnhold
* */
internal class Søknad{

}
