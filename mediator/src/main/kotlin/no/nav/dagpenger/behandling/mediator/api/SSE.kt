package no.nav.dagpenger.behandling.mediator.api

import io.ktor.http.CacheControl
import io.ktor.http.ContentType
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.cacheControl
import io.ktor.server.response.respondBytesWriter
import io.ktor.utils.io.writeStringUtf8
import kotlinx.coroutines.flow.Flow
import java.util.UUID

data class BehandlingSseEvent(
    val behandlingId: UUID,
    val handling: String,
)

suspend fun ApplicationCall.respondSse(hendelser: Flow<BehandlingSseEvent>) {
    response.cacheControl(CacheControl.NoCache(null))
    respondBytesWriter(contentType = ContentType.Text.EventStream) {
        hendelser.collect { event ->
            writeStringUtf8("id: ${event.behandlingId}\n")
            writeStringUtf8("event: ${event.handling}\n")
            writeStringUtf8("\n")
            flush()
        }
    }
}
