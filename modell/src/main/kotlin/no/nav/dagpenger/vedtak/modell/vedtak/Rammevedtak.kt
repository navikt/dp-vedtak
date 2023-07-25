package no.nav.dagpenger.vedtak.modell.vedtak

import no.nav.dagpenger.vedtak.modell.Dagpengerettighet
import no.nav.dagpenger.vedtak.modell.entitet.Beløp
import no.nav.dagpenger.vedtak.modell.entitet.Stønadsdager
import no.nav.dagpenger.vedtak.modell.entitet.Timer
import no.nav.dagpenger.vedtak.modell.vedtak.fakta.AntallStønadsdager
import no.nav.dagpenger.vedtak.modell.vedtak.fakta.Dagsats
import no.nav.dagpenger.vedtak.modell.vedtak.fakta.Egenandel
import no.nav.dagpenger.vedtak.modell.vedtak.fakta.Faktum
import no.nav.dagpenger.vedtak.modell.vedtak.fakta.Grunnlag
import no.nav.dagpenger.vedtak.modell.vedtak.fakta.VanligArbeidstidPerDag
import no.nav.dagpenger.vedtak.modell.vedtak.rettighet.Ordinær
import no.nav.dagpenger.vedtak.modell.vedtak.rettighet.Rettighet
import no.nav.dagpenger.vedtak.modell.visitor.VedtakVisitor
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class Rammevedtak(
    vedtakId: UUID = UUID.randomUUID(),
    behandlingId: UUID,
    vedtakstidspunkt: LocalDateTime = LocalDateTime.now(),
    virkningsdato: LocalDate,
    private val fakta: List<Faktum<*>>,
    private val rettigheter: List<Rettighet>,
) : Vedtak(
    vedtakId = vedtakId,
    behandlingId = behandlingId,
    vedtakstidspunkt = vedtakstidspunkt,
    utfall = true,
    virkningsdato = virkningsdato,
) {

    companion object {
        fun innvilgelse(
            behandlingId: UUID,
            virkningsdato: LocalDate,
            grunnlag: Beløp,
            dagsats: Beløp,
            stønadsdager: Stønadsdager,
            dagpengerettighet: Dagpengerettighet,
            vanligArbeidstidPerDag: Timer,
            egenandel: Beløp,
        ): Rammevedtak {
            val rettighet = when (dagpengerettighet) {
                Dagpengerettighet.Ordinær -> Ordinær(UUID.randomUUID(), virkningsdato, utfall = true)
                Dagpengerettighet.Permittering -> TODO()
                Dagpengerettighet.PermitteringFraFiskeindustrien -> TODO()
                Dagpengerettighet.ForskutterteLønnsgarantimidler -> TODO()
                Dagpengerettighet.Ingen -> TODO()
            }
            return Rammevedtak(
                behandlingId = behandlingId,
                virkningsdato = virkningsdato,
                fakta = listOf(
                    VanligArbeidstidPerDag(vanligArbeidstidPerDag),
                    Grunnlag(grunnlag),
                    Dagsats(dagsats),
                    AntallStønadsdager(stønadsdager),
                    Egenandel(egenandel),
                ),
                rettigheter = listOf(rettighet),
            )
        }
    }

    override fun accept(visitor: VedtakVisitor) {
        fakta.forEach {
            it.accept(visitor)
        }
    }
}
