package no.nav.dagpenger.vedtak

import no.nav.dagpenger.vedtak.modell.TemporalCollection
import no.nav.dagpenger.vedtak.modell.entitet.Timer
import no.nav.dagpenger.vedtak.modell.entitet.Timer.Companion.timer
import no.nav.dagpenger.vedtak.modell.hendelser.Rapporteringsdag
import no.nav.dagpenger.vedtak.modell.hendelser.Rapporteringshendelse
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class Vedtak2 private constructor(
    private val vedtakId: UUID = UUID.randomUUID(),
    private val behandlingId: UUID,
    private val vedtakstidspunkt: LocalDateTime = LocalDateTime.now(),
    private val virkningsdato: LocalDate,
    rettigheter: List<Rettighet>,

) {
    private val rettigheter = rettigheter.toMutableList()
    constructor(
        behandlingId: UUID,
        virkningsdato: LocalDate,
    ) : this(
        behandlingId = behandlingId,
        virkningsdato = virkningsdato,
        rettigheter = emptyList(),
    )

    fun rettighet(rettighet: Rettighet) {
        rettigheter.add(rettighet)
    }
    fun rettighet() = rettigheter.toList()

    fun håndter(rapporteringshendelse: Rapporteringshendelse) {
        TODO("Not yet implemented")
    }

    fun innenfor(dato: LocalDate): Boolean = dato >= virkningsdato
    fun virkingsdato(): LocalDate = virkningsdato
}

class Rettighet(
    private val tom: LocalDate? = null,
    internal val type: RettighetType,
    private val regel: Regel,

) {
    fun håndter(rapporteringshendelse: Rapporteringshendelse) {
        regel.håndter(rapporteringshendelse)
    }

    override fun toString(): String {
        return "Rettighet(tom=$tom, regel=$regel)"
    }

    enum class RettighetType {
        Ordinær,
    }
}

abstract class Regel(val type: Type) {
    abstract fun håndter(rapporteringshendelse: Rapporteringshendelse)

    enum class Type {
        Vilkår,
        Beregning,
    }
}

abstract class Beregningsregel : Regel(Type.Beregning)
abstract class Vilkår : Regel(Type.Vilkår)

class OrdinærTerskelRegel(val vanligArbeidstidPerDag: Timer) : Beregningsregel() {
    override fun håndter(rapporteringshendelse: Rapporteringshendelse) {
        println(this)
    }

    override fun toString(): String {
        return "OrdinærTerskelRegel(vanligArbeidstidPerDag=$vanligArbeidstidPerDag)"
    }
}

class PermitteringTerskelRegel(val vanligArbeidstidPerDag: Timer) : Beregningsregel() {
    override fun håndter(rapporteringshendelse: Rapporteringshendelse) {
        TODO("Not yet implemented")
    }
}

internal class VedtakHistorikk2(private val vedtak: MutableList<Vedtak2> = mutableListOf()) {

    private val rettigheter: MutableMap<Rettighet.RettighetType, TemporalCollection<Rettighet>> = mutableMapOf()

    fun leggTilVedtak(vedtak: Vedtak2) {
        this.vedtak.add(vedtak)
        genererHistorikk()
    }

    fun håndter(rapporteringshendelse: Rapporteringshendelse) {
        val rettighet = getRettigheter(rapporteringshendelse.somPeriode().endInclusive)
        println(rettighet)
        rettighet?.håndter(rapporteringshendelse)
    }

    private fun getRettigheter(dato: LocalDate): Rettighet? {
        return this.rettigheter[Rettighet.RettighetType.Ordinær]?.get(dato)
    }

    private fun genererHistorikk() {
        this.vedtak.forEach { vedtak ->
            vedtak.rettighet().forEach {
                leggtilRettighet(it.type, it, vedtak.virkingsdato())
            }
        }
    }

    private fun leggtilRettighet(type: Rettighet.RettighetType, rettighet: Rettighet, fraOgMed: LocalDate) {
        rettigheter.computeIfAbsent(type) {
            TemporalCollection()
        }.put(fraOgMed, rettighet)
    }
}

class VedtakgrublingTest {

    @Test
    fun `someti`() {
        val vedtak = Vedtak2(
            virkningsdato = LocalDate.now(),
            behandlingId = UUID.randomUUID(),
        ).also {
            it.rettighet(Rettighet(regel = OrdinærTerskelRegel(40.timer), type = Rettighet.RettighetType.Ordinær))
        }
        val vedtak2 = Vedtak2(
            virkningsdato = LocalDate.now().plusDays(4),
            behandlingId = UUID.randomUUID(),
        ).also {
            it.rettighet(Rettighet(regel = OrdinærTerskelRegel(40.timer), type = Rettighet.RettighetType.Ordinær))
        }

        val historikk = VedtakHistorikk2()
        historikk.leggTilVedtak(vedtak)
        historikk.leggTilVedtak(vedtak2)

        val rapporteringshendelse = Rapporteringshendelse(
            ident = "122",
            UUID.randomUUID(),
            rapporteringsdager = listOf(Rapporteringsdag(LocalDate.now().plusDays(5), false, 3)),
        )
        historikk.håndter(rapporteringshendelse)
    }
}
