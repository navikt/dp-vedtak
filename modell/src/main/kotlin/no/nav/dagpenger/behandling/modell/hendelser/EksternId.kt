package no.nav.dagpenger.behandling.modell.hendelser

import java.util.UUID

sealed class ExternId<T>(val id: T) {
    val type: T = id

    abstract fun kontekstMap(): Map<String, String>
}

class SøknadId(id: UUID) : ExternId<UUID>(id) {
    override fun kontekstMap() =
        mapOf(
            "søknadId" to id.toString(),
            "søknad_uuid" to id.toString(),
        )

//        "InnsendtSøknadsId" to mapOf(
//            "urn" to "urn:soknadid:${id}"
//            )
//        )
}
