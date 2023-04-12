package no.nav.dagpenger.vedtak.mediator.mottak

import mu.KotlinLogging
import mu.withLoggingContext
import no.nav.dagpenger.vedtak.mediator.PersonMediator
import no.nav.dagpenger.vedtak.modell.Dagpengerettighet
import no.nav.dagpenger.vedtak.modell.entitet.Timer.Companion.timer
import no.nav.dagpenger.vedtak.modell.hendelser.DagpengerAvslåttHendelse
import no.nav.dagpenger.vedtak.modell.hendelser.DagpengerInnvilgetHendelse
import no.nav.dagpenger.vedtak.modell.mengde.Enhet.Companion.arbeidsuker
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import java.time.LocalDate
import java.util.UUID

internal class SøknadBehandletMottak(rapidsConnection: RapidsConnection, private val personMediator: PersonMediator) :
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
        val dagpengerInnvilget = packet["innvilget"].asBoolean()

        withLoggingContext("behandlingId" to behandlingId.toString()) {
            val dagpengerBehandletHendelse = when {
                dagpengerInnvilget -> dagpengerInnvilgetHendelse(packet, behandlingId)
                else -> dagpengerAvslåttHendelse(packet, behandlingId)
            }

            logger.info { "Fått behandlingshendelse" }
            personMediator.håndter(dagpengerBehandletHendelse)
        }
    }

    private fun dagpengerAvslåttHendelse(packet: JsonMessage, behandlingId: UUID) = DagpengerAvslåttHendelse(
        ident = packet["ident"].asText(),
        behandlingId = behandlingId,
        virkningsdato = LocalDate.parse(packet["virkningsdato"].asText()),
    )

    private fun dagpengerInnvilgetHendelse(
        packet: JsonMessage,
        behandlingId: UUID,
    ) = DagpengerInnvilgetHendelse(
        ident = packet["ident"].asText(),
        behandlingId = behandlingId,
        virkningsdato = LocalDate.parse(packet["virkningsdato"].asText()),
        dagpengerettighet = Dagpengerettighet.valueOf(packet["dagpengerettighet"].asText()),
        dagsats = packet["dagsats"].decimalValue(),
        grunnlag = packet["grunnlag"].decimalValue(),
        stønadsperiode = packet["stønadsperiode"].asInt().arbeidsuker,
        vanligArbeidstidPerDag = packet["vanligArbeidstidPerDag"].asDouble().timer,
        antallVentedager = packet["antallVentedager"].asDouble(),
    )
}
