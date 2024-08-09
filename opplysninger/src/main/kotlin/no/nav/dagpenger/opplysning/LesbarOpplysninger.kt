package no.nav.dagpenger.opplysning

import java.util.UUID

interface LesbarOpplysninger {
    val id: UUID

    fun <T : Comparable<T>> finnOpplysning(opplysningstype: Opplysningstype<T>): Opplysning<T>

    fun har(opplysningstype: Opplysningstype<*>): Boolean

    fun finnAlle(opplysningstyper: List<Opplysningstype<*>>): List<Opplysning<*>>

    fun finnAlle(): List<Opplysning<*>>

    fun finnOpplysning(opplysningId: UUID): Opplysning<*>
}
