package no.nav.dagpenger.vedtak.modell

import no.nav.dagpenger.vedtak.modell.entitet.Timer
import no.nav.dagpenger.vedtak.modell.mengde.Stønadsperiode
import no.nav.dagpenger.vedtak.modell.visitor.VedtakVisitor
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

sealed class Vedtak(
    protected val vedtakId: UUID = UUID.randomUUID(),
    protected val vedtakstidspunkt: LocalDateTime = LocalDateTime.now(),
    protected val utfall: Boolean,
    protected val virkningsdato: LocalDate,
) {
    companion object {
        fun avslag(virkningsdato: LocalDate) = Avslag(virkningsdato = virkningsdato)

        fun innvilgelse(
            virkningsdato: LocalDate,
            grunnlag: BigDecimal,
            dagsats: BigDecimal,
            stønadsperiode: Stønadsperiode,
            dagpengerettighet: Dagpengerettighet,
            fastsattArbeidstidPerDag: Timer,
            gyldigTom: LocalDate? = null,
        ) = Rammevedtak(
            virkningsdato = virkningsdato,
            grunnlag = grunnlag,
            dagsats = dagsats,
            stønadsperiode = stønadsperiode,
            dagpengerettighet = dagpengerettighet, // TODO: Skal rettighetslista bare inneholde innvilgede rettigheter? Hva med avslag på utdanning f.eks.?
            fastsattArbeidstidPerDag = fastsattArbeidstidPerDag,
            gyldigTom = gyldigTom, // TODO: Noe mer fornuftig
        )
    }

    abstract fun accept(visitor: VedtakVisitor)
}

class Avslag(
    vedtakId: UUID = UUID.randomUUID(),
    vedtakstidspunkt: LocalDateTime = LocalDateTime.now(),
    virkningsdato: LocalDate,
) : Vedtak(vedtakId, vedtakstidspunkt, utfall = false, virkningsdato) {
    override fun accept(visitor: VedtakVisitor) {
        visitor.preVisitVedtak(vedtakId, virkningsdato, vedtakstidspunkt, utfall)
        visitor.postVisitVedtak(vedtakId, virkningsdato, vedtakstidspunkt, utfall, null) // TODO: Refaktorer visitorer
    }
}

class Rammevedtak(
    vedtakId: UUID = UUID.randomUUID(),
    vedtakstidspunkt: LocalDateTime = LocalDateTime.now(),
    virkningsdato: LocalDate,
    private val fastsattArbeidstidPerDag: Timer,
    private val grunnlag: BigDecimal,
    private val dagsats: BigDecimal,
    private val stønadsperiode: Stønadsperiode,
    private val dagpengerettighet: Dagpengerettighet,
    private val gyldigTom: LocalDate?,
) : Vedtak(vedtakId, vedtakstidspunkt, utfall = true, virkningsdato) {

    override fun accept(visitor: VedtakVisitor) {
        visitor.preVisitVedtak(vedtakId, virkningsdato, vedtakstidspunkt, utfall)
        visitor.visitRammeVedtak(
            fastsattArbeidstidPerDag = fastsattArbeidstidPerDag,
            grunnlag = grunnlag,
            dagsats = dagsats,
            stønadsperiode = stønadsperiode,
            dagpengerettighet = dagpengerettighet,
            gyldigTom = gyldigTom,
        )
        visitor.postVisitVedtak(vedtakId, virkningsdato, vedtakstidspunkt, utfall, gyldigTom)
    }
}
