package no.nav.dagpenger.behandling.modell.hendelser

import no.nav.dagpenger.avklaring.Avklaring
import no.nav.dagpenger.avklaring.Avklaringer
import no.nav.dagpenger.avklaring.Kontrollpunkt
import no.nav.dagpenger.opplysning.LesbarOpplysninger
import no.nav.dagpenger.opplysning.Opplysninger
import no.nav.dagpenger.opplysning.Regelkjøringsrapport

interface NoeSomBehandlingKanKalle : HendelseMedKontekst {
    val ident: String

    fun evaluer(opplysninger: Opplysninger): Regelkjøringsrapport

    fun kontrollpunkter(): List<Kontrollpunkt>

    fun avklaringer(
        avklaringer: Avklaringer,
        opplysninger: LesbarOpplysninger,
    ): List<Avklaring>

    fun harYtelse(opplysninger: LesbarOpplysninger): Boolean
}
