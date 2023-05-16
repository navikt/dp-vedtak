package no.nav.dagpenger.vedtak.mediator.mottak

import no.nav.dagpenger.vedtak.mediator.IHendelseMediator
import no.nav.dagpenger.vedtak.mediator.melding.HendelseMessage
import no.nav.dagpenger.vedtak.modell.hendelser.Rapporteringsdag
import no.nav.dagpenger.vedtak.modell.hendelser.Rapporteringshendelse
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.asLocalDate

internal class RapporteringBehandletHendelseMessage(private val packet: JsonMessage) : HendelseMessage(packet) {
    override val ident: String
        get() = packet["ident"].asText()

    private val hendelse = Rapporteringshendelse(
        ident = ident,
        rapporteringsId = packet["periodeId"].asUUID(),
        rapporteringsdager = packet["dager"].map { dag ->
            Rapporteringsdag(
                dato = dag["dato"].asLocalDate(),
                fravær = dag["fravær"].asBoolean(),
                timer = dag["timer"].asDouble(),
            )
        },
    )

    override fun behandle(mediator: IHendelseMediator, context: MessageContext) {
        mediator.behandle(hendelse, this, context)
    }
}
