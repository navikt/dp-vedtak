package no.nav.dagpenger.vedtak.mediator.mottak

import no.nav.dagpenger.vedtak.mediator.persistens.Melding
import no.nav.dagpenger.vedtak.modell.Dagpengerettighet
import no.nav.dagpenger.vedtak.modell.entitet.Timer.Companion.timer
import no.nav.dagpenger.vedtak.modell.hendelser.DagpengerAvslåttHendelse
import no.nav.dagpenger.vedtak.modell.hendelser.DagpengerInnvilgetHendelse
import no.nav.dagpenger.vedtak.modell.hendelser.SøknadBehandletHendelse
import no.nav.dagpenger.vedtak.modell.mengde.Enhet.Companion.arbeidsuker
import no.nav.helse.rapids_rivers.JsonMessage
import java.time.LocalDate
import java.util.UUID

internal class SøknadBehandletMelding(private val packet: JsonMessage) : Melding {
    val behandlingId = UUID.fromString(packet["behandlingId"].asText())
    private val ident = packet["ident"].asText()
    private val dagpengerInnvilget = packet["innvilget"].asBoolean()

    fun hendelse(): SøknadBehandletHendelse {
        return when {
            dagpengerInnvilget -> dagpengerInnvilgetHendelse(packet, behandlingId)
            else -> dagpengerAvslåttHendelse(packet, behandlingId)
        }
    }

    private fun dagpengerAvslåttHendelse(packet: JsonMessage, behandlingId: UUID) =
        DagpengerAvslåttHendelse(
            ident = ident,
            behandlingId = behandlingId,
            virkningsdato = LocalDate.parse(packet["virkningsdato"].asText()),
        )

    private fun dagpengerInnvilgetHendelse(
        packet: JsonMessage,
        behandlingId: UUID,
    ) = DagpengerInnvilgetHendelse(
        ident = ident,
        behandlingId = behandlingId,
        virkningsdato = LocalDate.parse(packet["virkningsdato"].asText()),
        dagpengerettighet = Dagpengerettighet.valueOf(packet["dagpengerettighet"].asText()),
        dagsats = packet["dagsats"].decimalValue(),
        grunnlag = packet["grunnlag"].decimalValue(),
        stønadsperiode = packet["stønadsperiode"].asInt().arbeidsuker,
        vanligArbeidstidPerDag = packet["vanligArbeidstidPerDag"].asDouble().timer,
        antallVentedager = packet["antallVentedager"].asDouble(),
    )

    override fun asJson(): String {
        TODO("Not yet implemented")
    }

    override fun eier() = ident
    override fun meldingId() = behandlingId.toString()
}
