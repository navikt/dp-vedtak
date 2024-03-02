package no.nav.dagpenger.regel

import kotlin.reflect.KProperty

object Behov {
    val OpptjeningsperiodeFraOgMed by StringConstant()
    val InntektId by StringConstant()
    val SisteAvsluttendeKalenderMåned by StringConstant()
}

class StringConstant {
    operator fun getValue(
        thisRef: Any?,
        property: KProperty<*>,
    ) = property.name
}
