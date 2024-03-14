package no.nav.dagpenger.behandling.mediator.repository

import no.nav.dagpenger.opplysning.Opplysninger
import java.util.UUID

interface OpplysningerRepository {
    fun hentOpplysninger(opplysningerId: UUID): Opplysninger?

    fun lagreOpplysninger(opplysninger: Opplysninger)

    fun lagreOpplysninger(opplysninger: List<Opplysninger>)
}
