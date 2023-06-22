package no.nav.dagpenger.vedtak.modell.vedtak

import no.nav.dagpenger.aktivitetslogg.Aktivitetskontekst
import no.nav.dagpenger.aktivitetslogg.SpesifikkKontekst
import no.nav.dagpenger.vedtak.modell.Dagpengerettighet
import no.nav.dagpenger.vedtak.modell.entitet.Beløp
import no.nav.dagpenger.vedtak.modell.entitet.Beløp.Companion.beløp
import no.nav.dagpenger.vedtak.modell.entitet.Stønadsdager
import no.nav.dagpenger.vedtak.modell.entitet.Timer
import no.nav.dagpenger.vedtak.modell.utbetaling.LøpendeRettighetDag
import no.nav.dagpenger.vedtak.modell.utbetaling.LøpendeRettighetDag.Companion.summer
import no.nav.dagpenger.vedtak.modell.visitor.VedtakVisitor
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

sealed class Vedtak(
    protected val vedtakId: UUID = UUID.randomUUID(),
    protected val behandlingId: UUID,
    protected val vedtakstidspunkt: LocalDateTime = LocalDateTime.now(),
    // @todo: Har alle vedtak utfall?
    protected val utfall: Boolean,
    protected val virkningsdato: LocalDate,
    protected val tilstand: Tilstand = Fattet,
) : Comparable<Vedtak> {
    companion object {
        fun avslag(behandlingId: UUID, virkningsdato: LocalDate) =
            Avslag(behandlingId = behandlingId, virkningsdato = virkningsdato)

        fun innvilgelse(
            behandlingId: UUID,
            virkningsdato: LocalDate,
            grunnlag: BigDecimal,
            dagsats: BigDecimal,
            stønadsdager: Stønadsdager,
            dagpengerettighet: Dagpengerettighet,
            vanligArbeidstidPerDag: Timer,
            egenandel: Beløp,
        ) = Rammevedtak(
            behandlingId = behandlingId,
            virkningsdato = virkningsdato,
            vanligArbeidstidPerDag = vanligArbeidstidPerDag,
            grunnlag = grunnlag,
            dagsats = dagsats,
            stønadsdager = stønadsdager,
            dagpengerettighet = dagpengerettighet,
            egenandel = egenandel,
        )

        fun løpendeRettighet(
            behandlingId: UUID,
            utfall: Boolean,
            virkningsdato: LocalDate,
            forbruk: Stønadsdager = Stønadsdager(dager = 0),
            rettighetsdager: List<LøpendeRettighetDag> = emptyList(),
            trukketEgenandel: Beløp = 0.beløp,
        ) =
            LøpendeRettighetVedtak(
                behandlingId = behandlingId,
                utfall = utfall,
                virkningsdato = virkningsdato,
                forbruk = forbruk,
                rettighetsdager = rettighetsdager,
                trukketEgenandel = trukketEgenandel,
            )

        internal fun Collection<Vedtak>.harBehandlet(behandlingId: UUID): Boolean =
            this.any { it.behandlingId == behandlingId }
    }

    abstract fun accept(visitor: VedtakVisitor)

    override fun compareTo(other: Vedtak): Int {
        return this.vedtakstidspunkt.compareTo(other.vedtakstidspunkt)
    }

    sealed class Tilstand(val tilstandnavn: TilstandNavn) : Aktivitetskontekst {

        enum class TilstandNavn {
            Fattet,
            Iverksatt,
        }
    }

    object Fattet : Tilstand(tilstandnavn = TilstandNavn.Fattet) {

        override fun toSpesifikkKontekst(): SpesifikkKontekst {
            TODO("Not yet implemented")
        }
    }

    object Iverksatt : Tilstand(tilstandnavn = TilstandNavn.Iverksatt) {

        override fun toSpesifikkKontekst(): SpesifikkKontekst {
            TODO("Not yet implemented")
        }
    }
}

class Avslag private constructor(
    vedtakId: UUID,
    behandlingId: UUID,
    vedtakstidspunkt: LocalDateTime,
    virkningsdato: LocalDate,
) : Vedtak(
    vedtakId = vedtakId,
    behandlingId = behandlingId,
    vedtakstidspunkt = vedtakstidspunkt,
    utfall = false,
    virkningsdato = virkningsdato,
) {

    constructor(behandlingId: UUID, virkningsdato: LocalDate) : this(
        vedtakId = UUID.randomUUID(),
        behandlingId = behandlingId,
        vedtakstidspunkt = LocalDateTime.now(),
        virkningsdato = virkningsdato,
    )

    override fun accept(visitor: VedtakVisitor) {
        visitor.visitAvslag(
            vedtakId = vedtakId,
            behandlingId = behandlingId,
            vedtakstidspunkt = vedtakstidspunkt,
            utfall = utfall,
            virkningsdato = virkningsdato,
        )
    }
}

class Rammevedtak(
    vedtakId: UUID = UUID.randomUUID(),
    behandlingId: UUID,
    vedtakstidspunkt: LocalDateTime = LocalDateTime.now(),
    virkningsdato: LocalDate,
    private val vanligArbeidstidPerDag: Timer,
    private val grunnlag: BigDecimal,
    private val dagsats: BigDecimal,
    private val stønadsdager: Stønadsdager,
    private val dagpengerettighet: Dagpengerettighet,
    private val egenandel: Beløp,
) : Vedtak(
    vedtakId = vedtakId,
    behandlingId = behandlingId,
    vedtakstidspunkt = vedtakstidspunkt,
    utfall = true,
    virkningsdato = virkningsdato,
) {

    override fun accept(visitor: VedtakVisitor) {
        visitor.visitRammevedtak(
            vedtakId = vedtakId,
            behandlingId = behandlingId,
            virkningsdato = virkningsdato,
            vedtakstidspunkt = vedtakstidspunkt,
            utfall = utfall,
            grunnlag = grunnlag,
            dagsats = dagsats,
            stønadsdager = stønadsdager,
            vanligArbeidstidPerDag = vanligArbeidstidPerDag,
            dagpengerettighet = dagpengerettighet,
            egenandel = egenandel,
            tilstand = tilstand,
        )
    }
}

class LøpendeRettighetVedtak(
    vedtakId: UUID = UUID.randomUUID(),
    behandlingId: UUID,
    vedtakstidspunkt: LocalDateTime = LocalDateTime.now(),
    utfall: Boolean,
    virkningsdato: LocalDate,
    private val forbruk: Stønadsdager,
    private val rettighetsdager: List<LøpendeRettighetDag>,
    private val trukketEgenandel: Beløp,
) : Vedtak(
    vedtakId = vedtakId,
    behandlingId = behandlingId,
    vedtakstidspunkt = vedtakstidspunkt,
    utfall = utfall,
    virkningsdato = virkningsdato,
) {
    override fun accept(visitor: VedtakVisitor) {
        val beløpTilUtbetaling = rettighetsdager.summer() - trukketEgenandel
        visitor.visitLøpendeRettighet(
            vedtakId = vedtakId,
            behandlingId = behandlingId,
            vedtakstidspunkt = vedtakstidspunkt,
            utfall = utfall,
            virkningsdato = virkningsdato,
            forbruk = forbruk,
            trukketEgenandel = trukketEgenandel,
            beløpTilUtbetaling = beløpTilUtbetaling,
            rettighetsdager = rettighetsdager,
        )
    }
}

class Stansvedtak(
    vedtakId: UUID = UUID.randomUUID(),
    behandlingId: UUID,
    vedtakstidspunkt: LocalDateTime = LocalDateTime.now(),
    virkningsdato: LocalDate,
) : Vedtak(vedtakId, behandlingId, vedtakstidspunkt, utfall = false, virkningsdato) {
    override fun accept(visitor: VedtakVisitor) {
        visitor.visitStans(
            vedtakId,
            behandlingId,
            virkningsdato,
            vedtakstidspunkt,
            utfall,
        )
    }
}
