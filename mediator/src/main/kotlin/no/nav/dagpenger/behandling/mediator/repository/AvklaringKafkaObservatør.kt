package no.nav.dagpenger.behandling.mediator.repository

import mu.KotlinLogging
import mu.withLoggingContext
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext

class AvklaringKafkaObservatør(
    private val rapid: MessageContext,
) : AvklaringRepositoryObserver {
    override fun nyAvklaring(nyAvklaringHendelse: AvklaringRepositoryObserver.NyAvklaringHendelse) {
        withLoggingContext(
            "avklaringId" to nyAvklaringHendelse.avklaring.id.toString(),
            "kode" to nyAvklaringHendelse.avklaring.kode.kode,
        ) {
            rapid.publish(
                nyAvklaringHendelse.ident,
                JsonMessage
                    .newMessage(
                        "NyAvklaring",
                        mapOf<String, Any>(
                            "ident" to nyAvklaringHendelse.ident,
                            "avklaringId" to nyAvklaringHendelse.avklaring.id,
                            "kode" to nyAvklaringHendelse.avklaring.kode.kode,
                        ) + nyAvklaringHendelse.kontekst.kontekstMap,
                    ).toJson(),
            )

            logger.info {
                "Publisert NyAvklaring med kode ${nyAvklaringHendelse.avklaring.kode.kode}"
            }
        }
    }

    override fun endretAvklaring(endretAvklaringHendelse: AvklaringRepositoryObserver.EndretAvklaringHendelse) {}

    private companion object {
        private val logger = KotlinLogging.logger {}
    }
}
