package no.nav.dagpenger.vedtak.modell

import no.nav.dagpenger.vedtak.modell.hendelse.Hendelse

abstract class Vedtak(hendelse: Hendelse, val avtale: Avtale?)

class Hovedvedtak(hendelse: Hendelse, avtale: Avtale?) : Vedtak(hendelse, avtale) {
    constructor(hendelse: Hendelse) : this(hendelse, null)
}

class Endringsvedtak(hendelse: Hendelse, avtale: Avtale?) : Vedtak(hendelse, avtale)

class Stansvedtak(hendelse: Hendelse, avtale: Avtale?) : Vedtak(hendelse, avtale)
