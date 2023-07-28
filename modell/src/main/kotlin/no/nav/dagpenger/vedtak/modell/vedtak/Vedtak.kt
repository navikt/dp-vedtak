package no.nav.dagpenger.vedtak.modell.vedtak

import no.nav.dagpenger.vedtak.modell.visitor.VedtakVisitor
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

abstract class Vedtak(
    protected val vedtakId: UUID = UUID.randomUUID(),
    protected val behandlingId: UUID,
    protected val vedtakstidspunkt: LocalDateTime = LocalDateTime.now(),
    protected val virkningsdato: LocalDate,
    protected val type: VedtakType,
) : Comparable<Vedtak> {
    enum class VedtakType {
        Ramme,
        Utbetaling,
        Avslag,
        Stans,
    }

    companion object {
        internal fun Collection<Vedtak>.harBehandlet(behandlingId: UUID): Boolean =
            this.any { it.behandlingId == behandlingId }
    }

    abstract fun accept(visitor: VedtakVisitor)

    override fun compareTo(other: Vedtak): Int {
        return this.vedtakstidspunkt.compareTo(other.vedtakstidspunkt)
    }
}
