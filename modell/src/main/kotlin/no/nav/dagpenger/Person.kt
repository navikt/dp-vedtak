package no.nav.dagpenger

import no.nav.dagpenger.hendelse.HarRettighetBehovHendelse
import no.nav.dagpenger.hendelse.NyRettighetHendelse

class Person private constructor(
    private val avtaler: MutableList<Avtale>,
    private val personIdent: PersonIdent
) {
    fun håndter(harRettighetBehovHendelse: HarRettighetBehovHendelse) {
        TODO("Not yet implemented")
    }
    fun håndter(nyRettighetHendelse: NyRettighetHendelse) {
        avtaler.add(Avtale(nyRettighetHendelse.søknad_uuid))
    }

    fun aktivAvtale(): Avtale? = avtaler.lastOrNull { it.erAktiv() }

    constructor(personIdent: PersonIdent) : this(mutableListOf(), personIdent)
    class PersonIdent(fnr: String) {}
}




