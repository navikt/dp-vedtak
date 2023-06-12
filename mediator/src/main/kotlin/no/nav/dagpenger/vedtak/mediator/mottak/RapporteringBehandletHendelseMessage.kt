package no.nav.dagpenger.vedtak.mediator.mottak

import com.fasterxml.jackson.databind.JsonNode
import no.nav.dagpenger.vedtak.mediator.IHendelseMediator
import no.nav.dagpenger.vedtak.mediator.melding.HendelseMessage
import no.nav.dagpenger.vedtak.modell.hendelser.Rapporteringsdag
import no.nav.dagpenger.vedtak.modell.hendelser.Rapporteringshendelse
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.asLocalDate
import kotlin.time.Duration

internal class RapporteringBehandletHendelseMessage(private val packet: JsonMessage) : HendelseMessage(packet) {
    override val ident: String get() = packet["ident"].asText()

    private val hendelse = Rapporteringshendelse(
        ident = ident,
        rapporteringsId = packet["rapporteringsId"].asUUID(),
        fom = packet["fom"].asLocalDate(),
        tom = packet["tom"].asLocalDate(),
        rapporteringsdager = packet["dager"].map { dag ->
            Rapporteringsdag(
                dato = dag["dato"].asLocalDate(),
                aktiviteter = aktiviteter(dag["aktiviteter"]),
            )
        },
    )

    override fun behandle(mediator: IHendelseMediator, context: MessageContext) {
        mediator.behandle(hendelse, this, context)
    }

    private fun aktiviteter(aktiviteter: JsonNode): List<Rapporteringsdag.Aktivitet> {
        return if (aktiviteter.isEmpty) {
            return listOf(
                Rapporteringsdag.Aktivitet(
                    type = Rapporteringsdag.Aktivitet.Type.Arbeid,
                    varighet = Duration.parseIsoString("PT0H"),
                ),
            )
        } else {
            aktiviteter.map { jsonAktivitet ->
                Rapporteringsdag.Aktivitet(
                    type = Rapporteringsdag.Aktivitet.Type.valueOf(jsonAktivitet["type"].asText()),
                    varighet = Duration.parseIsoString(jsonAktivitet["tid"].asText()),
                )
            }
        }
    }
}
