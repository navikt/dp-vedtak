package no.nav.dagpenger.vedtak.modell.vedtak

import no.nav.dagpenger.vedtak.modell.Dagpengerettighet
import no.nav.dagpenger.vedtak.modell.entitet.Beløp
import no.nav.dagpenger.vedtak.modell.entitet.Stønadsdager
import no.nav.dagpenger.vedtak.modell.entitet.Timer
import no.nav.dagpenger.vedtak.modell.vedtak.Vedtak.VedtakType.Ramme
import no.nav.dagpenger.vedtak.modell.vedtak.fakta.AntallStønadsdager
import no.nav.dagpenger.vedtak.modell.vedtak.fakta.Dagsats
import no.nav.dagpenger.vedtak.modell.vedtak.fakta.Egenandel
import no.nav.dagpenger.vedtak.modell.vedtak.fakta.Faktum
import no.nav.dagpenger.vedtak.modell.vedtak.fakta.VanligArbeidstidPerDag
import no.nav.dagpenger.vedtak.modell.vedtak.rettighet.Ordinær
import no.nav.dagpenger.vedtak.modell.vedtak.rettighet.Permittering
import no.nav.dagpenger.vedtak.modell.vedtak.rettighet.PermitteringFraFiskeindustrien
import no.nav.dagpenger.vedtak.modell.vedtak.rettighet.Rettighet
import no.nav.dagpenger.vedtak.modell.visitor.VedtakVisitor
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class Rammevedtak(
    vedtakId: UUID = UUID.randomUUID(),
    sakId: String,
    behandlingId: UUID,
    vedtakstidspunkt: LocalDateTime,
    virkningsdato: LocalDate,
    private val fakta: List<Faktum<*>>,
    private val rettigheter: List<Rettighet>,
) : Vedtak(
    vedtakId = vedtakId,
    sakId = sakId,
    behandlingId = behandlingId,
    vedtakstidspunkt = vedtakstidspunkt,
    virkningsdato = virkningsdato,
    type = Ramme,
) {

    companion object {
        fun innvilgelse(
            behandlingId: UUID,
            sakId: String,
            vedtakstidspunkt: LocalDateTime,
            virkningsdato: LocalDate,
            dagsats: Beløp,
            stønadsdager: Stønadsdager,
            dagpengerettighet: Dagpengerettighet,
            vanligArbeidstidPerDag: Timer,
            egenandel: Beløp,
        ): Rammevedtak {
            val rettighet = when (dagpengerettighet) {
                Dagpengerettighet.Ordinær -> Ordinær(utfall = true)
                Dagpengerettighet.Permittering -> Permittering(utfall = true)
                Dagpengerettighet.PermitteringFraFiskeindustrien -> PermitteringFraFiskeindustrien(utfall = true)
                Dagpengerettighet.ForskutterteLønnsgarantimidler -> TODO()
                Dagpengerettighet.Ingen -> TODO()
            }
            return Rammevedtak(
                sakId = sakId,
                behandlingId = behandlingId,
                vedtakstidspunkt = vedtakstidspunkt,
                virkningsdato = virkningsdato,
                fakta = listOf(
                    VanligArbeidstidPerDag(vanligArbeidstidPerDag),
                    Dagsats(dagsats),
                    AntallStønadsdager(stønadsdager),
                    Egenandel(egenandel),
                ),
                rettigheter = listOf(rettighet),
            )
        }
    }

    override fun accept(visitor: VedtakVisitor) {
        visitor.preVisitVedtak(
            vedtakId = vedtakId,
            behandlingId = behandlingId,
            virkningsdato = virkningsdato,
            vedtakstidspunkt = vedtakstidspunkt,
            type = type,
        )
        fakta.forEach {
            it.accept(visitor)
        }
        rettigheter.forEach {
            it.accept(visitor)
        }
        visitor.postVisitVedtak(
            vedtakId = vedtakId,
            behandlingId = behandlingId,
            virkningsdato = virkningsdato,
            vedtakstidspunkt = vedtakstidspunkt,
            type = type,
        )
    }
}
