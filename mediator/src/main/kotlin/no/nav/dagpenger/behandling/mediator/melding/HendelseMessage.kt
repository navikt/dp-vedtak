package no.nav.dagpenger.behandling.mediator.melding

import com.fasterxml.jackson.databind.JsonNode
import no.nav.dagpenger.behandling.mediator.IMessageMediator
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.asLocalDateTime
import java.util.UUID

internal abstract class HendelseMessage(
    private val packet: JsonMessage,
) {
    init {
        packet.interestedIn("@id", "@event_name", "@opprettet")
    }

    internal val id: UUID = packet["@id"].asUUID()
    private val navn = packet["@event_name"].asText()
    internal val opprettet = packet["@opprettet"].asLocalDateTime()
    internal abstract val ident: String

    internal abstract fun behandle(
        mediator: IMessageMediator,
        context: MessageContext,
    )

    internal fun lagreMelding(repository: HendelseRepository) {
        repository.lagreMelding(this, ident, id, toJson())
    }

    internal fun tracinginfo() =
        additionalTracinginfo(packet) +
            mapOf(
                "event_name" to navn,
                "id" to id.toString(),
                "opprettet" to opprettet.toString(),
            )

    protected open fun additionalTracinginfo(packet: JsonMessage): Map<String, String> = emptyMap()

    internal fun JsonNode.asUUID() = this.asText().let { UUID.fromString(it) }

    internal fun toJson() = packet.toJson()
}
