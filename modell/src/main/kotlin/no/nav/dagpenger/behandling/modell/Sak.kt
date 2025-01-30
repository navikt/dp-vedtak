package no.nav.dagpenger.behandling.modell

import no.nav.dagpenger.uuid.UUIDv7
import java.util.UUID

class Sak(
    val sakId: UUID,
) {
    constructor() : this(UUIDv7.ny())

    fun erÅpen(behandlinger: List<Behandling>): Boolean {
        return false
//        behandlinger.any { it.opplysninger().finnOpplysning(Opplysningstype.tekst(SakId, "sakId")) }
    }

    fun erAktiv(): Boolean = false
}

// fun List<Sak>.hentEllerOpprett() : Sak {
//    if (this.isEmpty()) {
//        return Sak()
//    }
//    return this.single { it.erAktiv() }
// }
