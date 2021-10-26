package no.nav.dagpenger.hendelse

import no.nav.dagpenger.Vedtak

class ProsessResultatHendelse(utfall: Boolean) : Hendelse {
    internal val vedtak = when (utfall) {
        true -> Vedtak.innvilg()
        false -> Vedtak.avslag()
    }
}
