package no.nav.dagpenger.vedtak.modell.visitor

import no.nav.dagpenger.vedtak.modell.vedtak.rettighet.Ordinær
import no.nav.dagpenger.vedtak.modell.vedtak.rettighet.Permittering
import no.nav.dagpenger.vedtak.modell.vedtak.rettighet.PermitteringFraFiskeindustrien

interface RettighetVisitor {
    fun visitOrdinær(ordinær: Ordinær) {}

    fun visitPermitteringFraFiskeindustrien(permitteringFraFiskeindustrien: PermitteringFraFiskeindustrien) {}

    fun visitPermittering(permittering: Permittering) {}
}
