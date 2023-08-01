package no.nav.dagpenger.vedtak.modell.vedtak

import no.nav.dagpenger.aktivitetslogg.Aktivitetskontekst
import no.nav.dagpenger.aktivitetslogg.SpesifikkKontekst
import no.nav.dagpenger.vedtak.modell.visitor.VedtakVisitor
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID

abstract class Vedtak(
    protected val vedtakId: UUID = UUID.randomUUID(),
    protected val behandlingId: UUID,
    protected val vedtakstidspunkt: LocalDateTime = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS),
    protected val virkningsdato: LocalDate,
    protected val type: VedtakType,
) : Comparable<Vedtak>, Aktivitetskontekst {
    enum class VedtakType {
        Ramme,
        Utbetaling,
        Avslag,
        Stans,
    }

    companion object {
        internal fun Collection<Vedtak>.harBehandlet(behandlingId: UUID): Boolean =
            this.any { it.behandlingId == behandlingId }

        private val etterVedtakstidspunkt = Comparator<Vedtak> { a, b -> a.vedtakstidspunkt.compareTo(b.vedtakstidspunkt) }
    }

    abstract fun accept(visitor: VedtakVisitor)

    override fun compareTo(other: Vedtak) = etterVedtakstidspunkt.compare(this, other)

    override fun toSpesifikkKontekst(): SpesifikkKontekst = SpesifikkKontekst(
        kontekstType = this.javaClass.simpleName,
        kontekstMap = mapOf(
            "vedtakId" to vedtakId.toString(),
        ),
    )
}
