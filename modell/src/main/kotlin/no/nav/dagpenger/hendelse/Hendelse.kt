package no.nav.dagpenger.hendelse

import no.nav.dagpenger.Hovedvedtak

interface Hendelse

internal class ProsessResultatHendelse(val utfall: Boolean) : Hendelse {
    val hovedvedtak
        get() = Hovedvedtak()
}

class ManglendeMeldekortHendelse : Hendelse
