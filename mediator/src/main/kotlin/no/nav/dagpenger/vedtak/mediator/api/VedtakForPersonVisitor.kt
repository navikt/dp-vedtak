package no.nav.dagpenger.vedtak.mediator.api

import no.nav.dagpenger.vedtak.api.models.RammeDTO
import no.nav.dagpenger.vedtak.api.models.UtbetalingDTO
import no.nav.dagpenger.vedtak.api.models.VedtakDTO
import no.nav.dagpenger.vedtak.modell.Person
import no.nav.dagpenger.vedtak.modell.SakId
import no.nav.dagpenger.vedtak.modell.entitet.Beløp
import no.nav.dagpenger.vedtak.modell.entitet.Periode
import no.nav.dagpenger.vedtak.modell.entitet.Stønadsdager
import no.nav.dagpenger.vedtak.modell.utbetaling.Utbetalingsdag
import no.nav.dagpenger.vedtak.modell.vedtak.Vedtak
import no.nav.dagpenger.vedtak.modell.visitor.PersonVisitor
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class VedtakForPersonVisitor(person: Person) : PersonVisitor {
    private val rammer = mutableListOf<RammeDTO>()
    private val mutableUtbetalinger = mutableMapOf<UUID, MutableMap<String, Any>>()

    init {
        person.accept(this)
    }

    fun vedtakListeDto(): VedtakDTO {
        val utbetalinger: List<UtbetalingDTO> = mutableUtbetalinger.map {
            UtbetalingDTO(
                vedtakId = it.key,
                fraOgMed = it.value["fraOgMed"] as LocalDate,
                tilOgMed = it.value["tilOgMed"] as LocalDate,
                sumUtbetalt = it.value["sumUtbetalt"] as Double,
            )
        }
        return VedtakDTO(rammer = rammer, utbetalinger = utbetalinger)
    }

    override fun preVisitVedtak(
        vedtakId: UUID,
        sakId: SakId,
        behandlingId: UUID,
        virkningsdato: LocalDate,
        vedtakstidspunkt: LocalDateTime,
        type: Vedtak.VedtakType,
    ) {
        if (type == Vedtak.VedtakType.Ramme) {
            this.rammer.add(RammeDTO(vedtakId = vedtakId, virkningsdato = virkningsdato))
        }

        if (type == Vedtak.VedtakType.Utbetaling) {
            val fraOgMed = virkningsdato.minusDays(13)
            val tilOgMed = virkningsdato

            mutableUtbetalinger[vedtakId] = mutableMapOf("fraOgMed" to fraOgMed, "tilOgMed" to tilOgMed)
        }
    }

    override fun visitUtbetalingsvedtak(
        vedtakId: UUID,
        periode: Periode,
        utfall: Boolean,
        forbruk: Stønadsdager,
        beløpTilUtbetaling: Beløp,
        utbetalingsdager: List<Utbetalingsdag>,
    ) {
        this.mutableUtbetalinger.map { (key, value) ->
            if (key == vedtakId) {
                value["sumUtbetalt"] = beløpTilUtbetaling.reflection { it }.toDouble()
            }
        }
    }
}
