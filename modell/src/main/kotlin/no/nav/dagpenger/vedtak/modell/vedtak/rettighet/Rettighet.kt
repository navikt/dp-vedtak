package no.nav.dagpenger.vedtak.modell.vedtak.rettighet

import java.time.LocalDate
import java.util.UUID

interface Rettighet {
    val id: UUID
    val virkningsdato: LocalDate
    val utfall: Boolean
}

abstract class Hovedrettighet() : Rettighet
class Ordinær(
    override val id: UUID,
    override val virkningsdato: LocalDate,
    override val utfall: Boolean,
) : Hovedrettighet()

abstract class Tilleggsrettighet() : Rettighet
class Barn(
    override val id: UUID,
    override val virkningsdato: LocalDate,
    override val utfall: Boolean,
) : Tilleggsrettighet()
