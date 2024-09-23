package no.nav.dagpenger.behandling.mediator.api

import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import java.util.UUID

internal data class OpplysningsSvar(
    val behandlingId: UUID,
    val opplysningId: UUID,
    val opplysningNavn: String,
    val ident: String,
    val verdi: Any,
    val saksbehandler: String,
) {
    fun toJson(): String =
        JsonMessage
            .newNeed(
                listOf(opplysningNavn),
                mapOf(
                    "@final" to true,
                    "@opplysningsbehov" to true,
                    "opplysningId" to opplysningId,
                    "behandlingId" to behandlingId,
                    "ident" to ident,
                    "@løsning" to
                        mapOf(
                            opplysningNavn to
                                mapOf(
                                    "verdi" to verdi,
                                    "@kilde" to
                                        mapOf(
                                            "saksbehandler" to saksbehandler,
                                        ),
                                ),
                        ),
                ),
            ).toJson()
}
