package no.nav.dagpenger.vedtak.modell.visitor

import no.nav.dagpenger.vedtak.modell.SakId

interface SakVisitor {
    fun visitSak(sakId: SakId) {}
}
