package no.nav.dagpenger

import no.nav.dagpenger.Vedtak.Companion.erAktiv
import no.nav.dagpenger.hendelse.ManglendeMeldekortHendelse
import no.nav.dagpenger.hendelse.ProsessResultatHendelse

class Person private constructor(
    private val vedtak: MutableList<Vedtak>
) {
    constructor() : this(mutableListOf())

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

    fun harDagpenger() = vedtak.any(::erAktiv)
}
