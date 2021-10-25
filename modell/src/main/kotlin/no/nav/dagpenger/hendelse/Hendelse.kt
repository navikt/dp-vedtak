package no.nav.dagpenger.hendelse

import no.nav.dagpenger.Hovedvedtak
import no.nav.dagpenger.Hovedvedtak.Utfall.Avslått
import no.nav.dagpenger.Hovedvedtak.Utfall.Innvilget

interface Hendelse

class ProsessResultatHendelse(utfall: Boolean) : Hendelse {

    internal val hovedvedtak
        get() = Hovedvedtak(utfall)
    private val utfall = when (utfall) {
        true -> Innvilget
        false -> Avslått
    }
}

class ManglendeMeldekortHendelse : Hendelse
