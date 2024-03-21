package no.nav.dagpenger.opplysning

import java.time.LocalDateTime
import java.util.UUID

sealed class Kilde(
    val id: UUID,
    // Når vi registrerte opplysningen
    val registrert: LocalDateTime,
    // Når opplysningen ble opprettet (på utsiden)
    val opprettet: LocalDateTime,
)

class Systemkilde(
    val meldingsreferanseId: UUID,
    opprettet: LocalDateTime,
    id: UUID = UUIDv7.ny(),
    registrert: LocalDateTime = LocalDateTime.now(),
) : Kilde(id, registrert, opprettet)

class Saksbehandlerkilde(
    val ident: String,
    opprettet: LocalDateTime = LocalDateTime.now(),
    id: UUID = UUIDv7.ny(),
    registrert: LocalDateTime = LocalDateTime.now(),
) : Kilde(id, registrert, opprettet)
