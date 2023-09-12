package no.nav.dagpenger.vedtak.mediator.mottak

import mu.KotlinLogging
import mu.withLoggingContext
import no.nav.dagpenger.vedtak.mediator.IHendelseMediator
import no.nav.dagpenger.vedtak.modell.PersonIdentifikator
import no.nav.dagpenger.vedtak.modell.vedtak.rettighet.Rettighet
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.MessageProblems
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import java.util.UUID

internal class RettighetBehandletMottak(
    rapidsConnection: RapidsConnection,
    private val hendelseMediator: IHendelseMediator,
) :
    River.PacketListener {

    companion object {
        private val logger = KotlinLogging.logger { }
    }

    init {
        River(rapidsConnection).apply {
            validate { it.demandValue("@event_name", "rettighet_behandlet_hendelse") }
            validate {
                it.requireKey("@id", "@opprettet")
                it.require("ident") { ident ->
                    PersonIdentifikator(ident.asText())
                }
                it.requireKey(
                    "behandlingId",
                    "sakId",
                    "Virkningsdato",
                    "utfall",
                )
                it.interestedIn("Rettighetstype") { rettighetstype ->
                    // todo: Ikke binde seg til enum så langt ute fra modellen
                    Rettighet.RettighetType.valueOf(rettighetstype.asText())
                }
                it.interestedIn(
                    "Dagsats",
                    "Grunnlag",
                    "Periode",
                    "Fastsatt vanlig arbeidstid",
                    "barnetillegg",
                )
            }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        val behandlingId = UUID.fromString(packet["behandlingId"].asText())
        withLoggingContext("behandlingId" to behandlingId.toString()) {
            val rettighetBehandletMelding = RettighetBehandletHendelseMessage(packet)
            logger.info { "Fått behandlingshendelse" }
            rettighetBehandletMelding.behandle(hendelseMediator, context)
        }
    }

    override fun onError(problems: MessageProblems, context: MessageContext) {
        logger.warn {
            "Kunne ikke lese 'rettighet_behandlet_hendelse', problemer:\n${problems.toExtendedReport()}"
        }
    }
}
