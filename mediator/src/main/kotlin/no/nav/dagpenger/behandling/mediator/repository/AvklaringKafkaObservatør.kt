package no.nav.dagpenger.behandling.mediator.repository

import mu.KotlinLogging
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext

class AvklaringKafkaObservatør(
    private val rapid: MessageContext,
) : AvklaringRepositoryObserver {
    override fun nyAvklaring(hendelse: AvklaringRepositoryObserver.NyAvklaringHendelse) {
        rapid.publish(
            hendelse.ident,
            JsonMessage
                .newMessage(
                    "NyAvklaring",
                    mapOf<String, Any>(
                        "ident" to hendelse.ident,
                        "avklaringId" to hendelse.avklaring.id,
                        "kode" to hendelse.avklaring.kode.kode,
                    ) + hendelse.kontekst.kontekstMap,
                ).toJson(),
        )

        logger.info { "Publisert NyAvklaring for avklaringId=${hendelse.avklaring.id}" }
    }

    override fun endretAvklaring(endretAvklaringHendelse: AvklaringRepositoryObserver.EndretAvklaringHendelse) {}

    private companion object {
        private val logger = KotlinLogging.logger {}
    }
}
