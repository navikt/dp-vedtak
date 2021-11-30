package no.nav.dagpenger.vedtak.modell.hendelse

import no.nav.dagpenger.vedtak.modell.tid.quantity.Tid

class InnvilgetProsessresultatHendelse(val sats: Double, val periode: Tid) :
    Hendelse
