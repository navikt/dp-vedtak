package no.nav.dagpenger.vedtak.mediator.mottak

import no.nav.dagpenger.vedtak.mediator.persistens.HendelseMessage
import no.nav.dagpenger.vedtak.modell.Dagpengerettighet
import no.nav.dagpenger.vedtak.modell.entitet.Timer.Companion.timer
import no.nav.dagpenger.vedtak.modell.hendelser.DagpengerAvslåttHendelse
import no.nav.dagpenger.vedtak.modell.hendelser.DagpengerInnvilgetHendelse
import no.nav.dagpenger.vedtak.modell.hendelser.SøknadBehandletHendelse
import no.nav.dagpenger.vedtak.modell.mengde.Enhet.Companion.arbeidsuker
import no.nav.helse.rapids_rivers.JsonMessage
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

internal class SøknadBehandletHendelseMessage(private val packet: JsonMessage) : HendelseMessage {
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
            virkningsdato = LocalDate.parse(packet["Virkningsdato"].asText()),
        )

    private fun dagpengerInnvilgetHendelse(
        packet: JsonMessage,
        behandlingId: UUID,
    ) = DagpengerInnvilgetHendelse(
        ident = ident,
        behandlingId = behandlingId,
        virkningsdato = LocalDate.parse(packet["Virkningsdato"].asText()),
        dagpengerettighet = Dagpengerettighet.valueOf(packet["Rettighetstype"].asText()),
        dagsats = packet["Dagsats"].decimalValue(),
        grunnlag = packet["Grunnlag"].decimalValue(),
        stønadsperiode = packet["Periode"].asInt().arbeidsuker,
        vanligArbeidstidPerDag = packet["Fastsatt vanlig arbeidstid"].asDouble().timer,
        egenandel = when (Dagpengerettighet.valueOf(packet["Rettighetstype"].asText())) {
            Dagpengerettighet.Ordinær, Dagpengerettighet.Permittering -> packet["Dagsats"].decimalValue() * BigDecimal(3)
            else -> BigDecimal(0)
        }, // @todo: hva/hvem skal sette egenandel?
    )

    override fun asJson(): String {
        TODO("Not yet implemented")
    }

    override fun eier() = ident
    override fun meldingId() = behandlingId.toString()
}
