package no.nav.dagpenger.vedtak.modell

import no.nav.dagpenger.vedtak.kontomodell.TemporalCollection
import no.nav.dagpenger.vedtak.modell.hendelser.Ordinær
import no.nav.dagpenger.vedtak.modell.visitor.VedtakHistorikkVisitor
import java.util.UUID

class VedtakHistorikk private constructor(private val vedtak: MutableList<Vedtak>) {

    private val satser = TemporalCollection<VedtakFakta<Beløp>>()

    constructor() : this(mutableListOf())

    fun leggTilVedtak(ordinær: Ordinær) {
        vedtak.add(
            Vedtak(
                virkningsdato = ordinær.virkningsdato,
                beslutningstidspunkt = ordinær.beslutningstidspunkt,
                vedtakId = VedtakIdentifikator(UUID.randomUUID())
            ).also {
                satser.put(ordinær.virkningsdato, VedtakFakta(it.id(), ordinær.dagsats))
            }
        )
    }

    fun beregn(aktivitetsTidslinje: AktivitetsTidslinje): BeregnetTidslinje {
        val rapporteringsPeriode = aktivitetsTidslinje.rapporteringsPerioder.first()
        return BeregnetTidslinje(rapporteringsPeriode.dager.map { BeregnetDag(it.dato, beløp = 500) })
    }

    fun accept(visitor: VedtakHistorikkVisitor) {
        visitor.preVisitVedtakHistorikk()
        vedtak.forEach { it.accept(visitor) }
        satser.historikk().forEach {
            visitor.visitDagsatsHistorikk(it.key.toLocalDate(), it.value.verdi)
        }
        visitor.postVisitVedtakHistorikk()
    }

    class VedtakFakta<T>(vedtakId: VedtakIdentifikator, val verdi: T)
}
