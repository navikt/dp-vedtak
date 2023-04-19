package no.nav.dagpenger.vedtak.mediator.mottak

import mu.KotlinLogging
import mu.withLoggingContext
import no.nav.dagpenger.vedtak.mediator.MeldingMediator
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.MessageProblems
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import java.util.UUID

internal class SøknadBehandletMottak(
    rapidsConnection: RapidsConnection,
    private val meldingMediator: MeldingMediator,
) :
    River.PacketListener {

    companion object {
        private val logger = KotlinLogging.logger { }
    }

    init {
        River(rapidsConnection).apply {
            validate { it.demandValue("@event_name", "søknad_behandlet_hendelse") }
            validate {
                it.requireKey(
                    "ident",
                    "behandlingId",
                    "virkningsdato",
                    "innvilget",
                )
                it.interestedIn(
                    "dagpengerettighet",
                    "dagsats",
                    "grunnlag",
                    "stønadsperiode",
                    "vanligArbeidstidPerDag",
                    "antallVentedager",
                    "barnetillegg",
                )
            }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        val behandlingId = UUID.fromString(packet["behandlingId"].asText())
        withLoggingContext("behandlingId" to behandlingId.toString()) {
            val søknadBehandletMelding = SøknadBehandletMelding(packet)
            logger.info { "Fått behandlingshendelse" }
            meldingMediator.håndter(søknadBehandletMelding)
        }
    }

    override fun onError(problems: MessageProblems, context: MessageContext) {
        logger.warn {
            "Kunne ikke lese 'søknad_behandlet_hendelse', problemer:\n${problems.toExtendedReport()}"
        }
    }
}
