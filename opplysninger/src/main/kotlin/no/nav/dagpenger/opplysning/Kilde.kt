package no.nav.dagpenger.opplysning

import no.nav.dagpenger.uuid.UUIDv7
import java.time.LocalDateTime
import java.util.UUID

sealed class Kilde(
    val meldingsreferanseId: UUID,
    val id: UUID,
    // Når vi registrerte opplysningen
    val registrert: LocalDateTime,
    // Når opplysningen ble opprettet (på utsiden)
    val opprettet: LocalDateTime,
)

class Systemkilde(
    meldingsreferanseId: UUID,
    opprettet: LocalDateTime,
    id: UUID = UUIDv7.ny(),
    registrert: LocalDateTime = LocalDateTime.now(),
) : Kilde(meldingsreferanseId, id, registrert, opprettet)

class Saksbehandlerkilde(
    meldingsreferanseId: UUID,
    val saksbehandler: Saksbehandler,
    opprettet: LocalDateTime = LocalDateTime.now(),
    id: UUID = UUIDv7.ny(),
    registrert: LocalDateTime = LocalDateTime.now(),
) : Kilde(meldingsreferanseId, id, registrert, opprettet) {
    @Deprecated("Bruk constructor med Saksbehandler")
    constructor(
        meldingsreferanseId: UUID,
        saksbehandler: String,
    ) : this(meldingsreferanseId, Saksbehandler(saksbehandler))
}

data class Saksbehandler(
    val ident: String,
)
