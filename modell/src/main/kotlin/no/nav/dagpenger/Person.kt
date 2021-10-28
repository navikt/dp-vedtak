package no.nav.dagpenger

import no.nav.dagpenger.Vedtak.Companion.erAktiv
import no.nav.dagpenger.hendelse.GjenopptakHendelse
import no.nav.dagpenger.hendelse.ManglendeMeldekortHendelse
import no.nav.dagpenger.hendelse.ProsessResultatHendelse

class Person private constructor(
    private val vedtak: MutableList<Vedtak>,
    private val personIdent: PersonIdent
) {
    constructor(personIdent: PersonIdent) : this(mutableListOf(), personIdent)

    fun håndter(hendelse: ProsessResultatHendelse) {
        if (vedtak.none {
            it.håndter(hendelse)
        }
        ) {
            vedtak.add(hendelse.vedtak)
        }
    }

    fun håndter(hendelse: ManglendeMeldekortHendelse) {
        vedtak.forEach { it.håndter(hendelse) }
    }

    fun håndter(hendelse: GjenopptakHendelse) {
        vedtak.forEach { it.håndter(hendelse) }
    }

    fun harDagpenger() = vedtak.any(::erAktiv)
    fun rettighetTilhørendePerson(): Rettighet? = vedtak.map{ it.hentKandidatForGjennopptak()}.firstOrNull()


    class PersonIdent(fnr: String){}
}
