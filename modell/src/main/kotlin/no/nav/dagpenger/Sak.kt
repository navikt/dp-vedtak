package no.nav.dagpenger

class Sak internal constructor(private val person: Person,
                               private val søknadsreferanse: String) {
    val vedtak = listOf<Vedtak>()


    companion object {
        fun List<Sak>.harTilknyttetSøknad(søknadsreferanse: String) = this.filter { it.harSøknad(søknadsreferanse) }.size == 1
    }

    fun harSøknad(søknadsreferanse: String) = this.søknadsreferanse == søknadsreferanse


}
