package no.nav.dagpenger

import no.nav.dagpenger.hendelse.ManglendeMeldekortHendelse
import no.nav.dagpenger.hendelse.ProsessResultatHendelse

internal class Hovedvedtak private constructor(
    private var tilstand: Tilstand,
    private val endringer: MutableList<Vedtak>,
    private val sats: Int,
    omgjortAv: Vedtak?
) : Vedtak(omgjortAv) {
    constructor() : this(Tilstand.Aktiv, mutableListOf(), 0, null)

    companion object {
        fun erAktiv(vedtak: Hovedvedtak) = vedtak.tilstand == Tilstand.Aktiv
    }

    fun håndter(hendelse: ProsessResultatHendelse): Boolean {
        tilstand.håndter(this, hendelse)
        return true
    }

    fun håndter(hendelse: ManglendeMeldekortHendelse) {
        tilstand.håndter(this, hendelse)
    }

    private interface Tilstand {
        fun håndter(hovedvedtak: Hovedvedtak, hendelse: ManglendeMeldekortHendelse) {}
        fun håndter(hovedvedtak: Hovedvedtak, hendelse: ProsessResultatHendelse) {}

        object Aktiv : Tilstand
        object Inaktiv : Tilstand
        object Avsluttet : Tilstand
    }
}
