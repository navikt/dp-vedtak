package no.nav.dagpenger.regel

import kotlin.reflect.KProperty

object Behov {
    val OpptjeningsperiodeFraOgMed by NamedValueDelegate()
    val InntektId by NamedValueDelegate()
}

class NamedValueDelegate {
    operator fun getValue(
        thisRef: Any?,
        property: KProperty<*>,
    ) = property.name
}
