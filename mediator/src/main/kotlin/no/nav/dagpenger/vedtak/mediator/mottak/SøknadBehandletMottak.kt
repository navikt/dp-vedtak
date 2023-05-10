package no.nav.dagpenger.vedtak.mediator.mottak

import mu.KotlinLogging
import mu.withLoggingContext
import no.nav.dagpenger.vedtak.mediator.IHendelseMediator
import no.nav.dagpenger.vedtak.modell.Dagpengerettighet
import no.nav.dagpenger.vedtak.modell.PersonIdentifikator
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.MessageProblems
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import java.util.UUID

internal class SøknadBehandletMottak(
    rapidsConnection: RapidsConnection,
    private val hendelseMediator: IHendelseMediator,
) :
    River.PacketListener {

    companion object {
        private val logger = KotlinLogging.logger { }
    }

    init {
        River(rapidsConnection).apply {
            validate { it.demandValue("@event_name", "søknad_behandlet_hendelse") }
            validate {
                it.require("ident") { ident ->
                    PersonIdentifikator(ident.asText())
                }
                it.requireKey(
                    "behandlingId",
                    "Virkningsdato",
                    "innvilget",
                )
                it.interestedIn("Rettighetstype") { rettighetstype ->
                    Dagpengerettighet.valueOf(rettighetstype.asText())
                }
                it.interestedIn(
                    "Dagsats",
                    "Grunnlag",
                    "Periode",
                    "Fastsatt vanlig arbeidstid",
                    "egenandel",
                    "barnetillegg",
                )
            }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        val behandlingId = UUID.fromString(packet["behandlingId"].asText())
        withLoggingContext("behandlingId" to behandlingId.toString()) {
            val søknadBehandletMelding = SøknadBehandletHendelseMessage(packet)
            logger.info { "Fått behandlingshendelse" }
            søknadBehandletMelding.behandle(hendelseMediator, context)
        }
    }

    override fun onError(problems: MessageProblems, context: MessageContext) {
        logger.warn {
            "Kunne ikke lese 'søknad_behandlet_hendelse', problemer:\n${problems.toExtendedReport()}"
        }
    }
}
