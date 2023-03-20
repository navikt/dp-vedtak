package no.nav.dagpenger.vedtak.modell.visitor

import no.nav.dagpenger.vedtak.modell.Dagpengerettighet
import no.nav.dagpenger.vedtak.modell.entitet.Timer
import no.nav.dagpenger.vedtak.modell.mengde.Stønadsperiode
import no.nav.dagpenger.vedtak.modell.mengde.Tid
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

interface VedtakVisitor {
    fun preVisitVedtak(vedtakId: UUID, virkningsdato: LocalDate, vedtakstidspunkt: LocalDateTime, utfall: Boolean) {}
    fun visitRammeVedtak(
        grunnlag: BigDecimal,
        dagsats: BigDecimal,
        stønadsperiode: Stønadsperiode,
        fastsattArbeidstidPerDag: Timer,
        dagpengerettighet: Dagpengerettighet,
        gyldigTom: LocalDate?,
    ) {}

    fun visitForbruk(forbruk: Tid) {}

    fun postVisitVedtak(
        vedtakId: UUID,
        virkningsdato: LocalDate,
        vedtakstidspunkt: LocalDateTime,
        utfall: Boolean,
        gyldigTom: LocalDate?,
    ) {}
}
