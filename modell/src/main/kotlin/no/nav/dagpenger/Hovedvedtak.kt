package no.nav.dagpenger

import no.nav.dagpenger.Hovedvedtak.Tilstand.Aktiv
import no.nav.dagpenger.Hovedvedtak.Tilstand.Avsluttet
import no.nav.dagpenger.Hovedvedtak.Utfall.Avslått
import no.nav.dagpenger.Hovedvedtak.Utfall.Innvilget
import no.nav.dagpenger.hendelse.ManglendeMeldekortHendelse
import no.nav.dagpenger.hendelse.ProsessResultatHendelse

internal class Hovedvedtak private constructor(
    private val utfall: Utfall,
    private val endringer: MutableList<Vedtak>,
    private val sats: Int,
    omgjortAv: Vedtak?
) : Vedtak(omgjortAv) {

    private var tilstand: Tilstand = when (utfall) {
        Avslått -> Avsluttet
        Innvilget -> Aktiv
    }

    constructor(utfall: Utfall) : this(
        utfall,
        mutableListOf(),
        0,
        null
    )

    companion object {
        fun erAktiv(vedtak: Hovedvedtak) = vedtak.tilstand == Aktiv
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

    enum class Utfall {
        Innvilget, Avslått
    }
}
