package no.nav.dagpenger.opplysning.verdier

import java.math.BigDecimal
import no.nav.dagpenger.inntekt.v1.Inntekt as InntektV1

class Inntekt(
    val verdi: InntektV1,
) : Comparable<Inntekt> {
    val id get() = verdi.inntektsId

    override fun compareTo(other: Inntekt): Int = verdi.inntektsId.compareTo(other.verdi.inntektsId)

    override fun hashCode(): Int = verdi.hashCode()

    override fun equals(other: Any?): Boolean = other is Inntekt && verdi == other.verdi

    fun sum(): BigDecimal = verdi.inntektsListe.map { it.klassifiserteInntekter.sumOf { it.beløp } }.sumOf { it }
}
