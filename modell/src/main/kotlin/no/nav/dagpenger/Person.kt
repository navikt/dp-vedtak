package no.nav.dagpenger

import no.nav.dagpenger.Hovedvedtak.Companion.erAktiv
import no.nav.dagpenger.hendelse.ProsessResultatHendelse

class Person private constructor(
    private val vedtak: MutableList<Hovedvedtak>
) {
    constructor() : this(mutableListOf())

    fun håndter(hendelse: ProsessResultatHendelse) {

        if (vedtak.none {
            it.håndter(hendelse)
        }
        ) { vedtak.add(hendelse.hovedvedtak) }
    }

    fun harDagpenger() = vedtak.any(::erAktiv)
}
