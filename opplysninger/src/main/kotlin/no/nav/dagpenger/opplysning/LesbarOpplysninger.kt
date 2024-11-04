package no.nav.dagpenger.opplysning

import java.time.LocalDate
import java.util.UUID

interface LesbarOpplysninger {
    val id: UUID

    fun <T : Comparable<T>> finnOpplysning(opplysningstype: Opplysningstype<T>): Opplysning<T>

    fun <T : Comparable<T>> finnOpplysninger(opplysningstype: Opplysningstype<T>): List<Opplysning<T>>

    fun har(opplysningstype: Opplysningstype<*>): Boolean

    fun mangler(opplysningstype: Opplysningstype<*>): Boolean = !har(opplysningstype)

    fun finnAlle(opplysningstyper: List<Opplysningstype<*>>): List<Opplysning<*>>

    fun finnAlle(): List<Opplysning<*>>

    fun finnOpplysning(opplysningId: UUID): Opplysning<*>

    fun forDato(gjelderFor: LocalDate): LesbarOpplysninger
}
