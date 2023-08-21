package no.nav.dagpenger.vedtak.modell.vedtak.rettighet

import no.nav.dagpenger.vedtak.modell.visitor.RettighetVisitor

interface Rettighet {

    val utfall: Boolean
    val type: RettighetType

    enum class RettighetType {
        Ordinær,
        PermitteringFraFiskeindustrien,
        Permittering,
    }
    fun accept(visitor: RettighetVisitor)
}

abstract class Hovedrettighet() : Rettighet

internal object IngenRettighet : Hovedrettighet() {
    override val utfall: Boolean
        get() = false
    override val type: Rettighet.RettighetType
        get() = throw IllegalArgumentException("Ingen rettighet har ingen type")

    override fun accept(visitor: RettighetVisitor) {}
}

class Ordinær(
    override val utfall: Boolean,
) : Hovedrettighet() {

    override val type: Rettighet.RettighetType
        get() = Rettighet.RettighetType.Ordinær
    override fun accept(visitor: RettighetVisitor) {
        visitor.visitOrdinær(this)
    }
}

class PermitteringFraFiskeindustrien(
    override val utfall: Boolean,
) : Hovedrettighet() {

    override val type: Rettighet.RettighetType
        get() = Rettighet.RettighetType.PermitteringFraFiskeindustrien
    override fun accept(visitor: RettighetVisitor) {
        visitor.visitPermitteringFraFiskeindustrien(this)
    }
}

class Permittering(
    override val utfall: Boolean,
) : Hovedrettighet() {

    override val type: Rettighet.RettighetType
        get() = Rettighet.RettighetType.Permittering
    override fun accept(visitor: RettighetVisitor) {
        visitor.visitPermittering(this)
    }
}

abstract class Tilleggsrettighet() : Rettighet
