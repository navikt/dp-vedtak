package no.nav.dagpenger.vedtak.modell.vedtak.rettighet

import no.nav.dagpenger.vedtak.modell.visitor.RettighetVisitor
import java.util.UUID

interface Rettighet {

    val id: UUID
    val utfall: Boolean
    fun accept(visitor: RettighetVisitor)
}

abstract class Hovedrettighet() : Rettighet
class Ordinær(
    override val id: UUID,
    override val utfall: Boolean,
) : Hovedrettighet() {
    override fun accept(visitor: RettighetVisitor) {
        visitor.visitOrdinær(this)
    }
}

class PermitteringFraFiskeindustrien(
    override val id: UUID,
    override val utfall: Boolean,
) : Hovedrettighet() {
    override fun accept(visitor: RettighetVisitor) {
        visitor.visitPermitteringFraFiskeindustrien(this)
    }
}

class Permittering(
    override val id: UUID,
    override val utfall: Boolean,
) : Hovedrettighet() {
    override fun accept(visitor: RettighetVisitor) {
        visitor.visitPermittering(this)
    }
}

abstract class Tilleggsrettighet() : Rettighet
class Barn(
    override val id: UUID,
    override val utfall: Boolean,
) : Tilleggsrettighet() {
    override fun accept(visitor: RettighetVisitor) {
        TODO("Not yet implemented")
    }
}
