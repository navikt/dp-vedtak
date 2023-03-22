package no.nav.dagpenger.vedtak.modell.vilkår

import no.nav.dagpenger.vedtak.modell.Dagpengerettighet
import no.nav.dagpenger.vedtak.modell.Person
import no.nav.dagpenger.vedtak.modell.entitet.Periode
import no.nav.dagpenger.vedtak.modell.entitet.Prosent
import no.nav.dagpenger.vedtak.modell.entitet.Timer
import no.nav.dagpenger.vedtak.modell.hendelser.Rapporteringshendelse
import no.nav.dagpenger.vedtak.modell.mengde.Stønadsperiode
import no.nav.dagpenger.vedtak.modell.rapportering.Arbeidsdag
import no.nav.dagpenger.vedtak.modell.rapportering.Dag
import no.nav.dagpenger.vedtak.modell.rapportering.Dag.Companion.summer
import no.nav.dagpenger.vedtak.modell.rapportering.Helgedag
import no.nav.dagpenger.vedtak.modell.visitor.PersonVisitor
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

internal class LøpendeStønadsperiodeVilkår(private val person: Person) :
    Vilkårsvurdering<LøpendeStønadsperiodeVilkår>(IkkeVurdert) {

    object IkkeVurdert : Tilstand.IkkeVurdert<LøpendeStønadsperiodeVilkår>() {
        override fun håndter(
            rapporteringsHendelse: Rapporteringshendelse,
            tellendeDager: List<Dag>,
            vilkårsvurdering: LøpendeStønadsperiodeVilkår,
        ) {
            val harGjenstående =
                HarGjenstående(vilkårsvurdering.person, rapporteringsHendelse.somPeriode()).harGjenstående()
            val underTerskel =
                HarArbeidetUnderTerskel(vilkårsvurdering.person, rapporteringsHendelse.somPeriode(), tellendeDager).underTerskel()
            if (harGjenstående && underTerskel) {
                vilkårsvurdering.endreTilstand(nyTilstand = Oppfylt)
            } else {
                vilkårsvurdering.endreTilstand(nyTilstand = IkkeOppfylt)
            }
        }
    }

    object Oppfylt : Tilstand.Oppfylt<LøpendeStønadsperiodeVilkår>()

    object IkkeOppfylt : Tilstand.IkkeOppfylt<LøpendeStønadsperiodeVilkår>()

    override fun <T> implementasjon(block: LøpendeStønadsperiodeVilkår.() -> T): T {
        return this.block()
    }

    private class HarGjenstående(person: Person, private val periode: Periode) : PersonVisitor {
        init {
            person.accept(this)
        }

        fun harGjenstående() = harGjenstående && harVedtak

        private var harGjenstående = false
        private var harVedtak = false

        override fun visitGjenståendeStønadsperiode(gjenståendePeriode: Stønadsperiode) {
            harGjenstående = true
        }

        override fun preVisitVedtak(
            vedtakId: UUID,
            behandlingId: UUID,
            virkningsdato: LocalDate,
            vedtakstidspunkt: LocalDateTime,
            utfall: Boolean,
        ) {
            if (virkningsdato <= periode.endInclusive) {
                harVedtak = true
            }
        }
    }

    private class HarArbeidetUnderTerskel(person: Person, val periode: Periode, val tellendeDager: List<Dag>) : PersonVisitor {

        private val arbeidsdager = mutableListOf<Dag>()
        lateinit var virkningsdato: LocalDate
        var harDagpengevedtak = false

        init {
            person.accept(this)
        }

        fun underTerskel(): Boolean {
            val arbeidstimer = tellendeDager.summer()
            val fastsattarbeidstidForPeriode =
                (fastsattArbeidstidPerDag * tellendeDager.filterIsInstance<Arbeidsdag>().size)

            if (arbeidstimer.div(fastsattarbeidstidForPeriode) <= Prosent(50.0)) {
                return true
            }

            return false
        }

        lateinit var fastsattArbeidstidPerDag: Timer

        override fun visitRammeVedtak(
            grunnlag: BigDecimal,
            dagsats: BigDecimal,
            stønadsperiode: Stønadsperiode,
            vanligArbeidstidPerDag: Timer,
            dagpengerettighet: Dagpengerettighet,
        ) {
            this.fastsattArbeidstidPerDag = vanligArbeidstidPerDag
            harDagpengevedtak = true // TODO: Burde se om Dagpengerettighet = OrdinæreDagpenger?
        }

        override fun visitArbeidsdag(arbeidsdag: Arbeidsdag) {
            if (arbeidsdag in periode) {
                arbeidsdager.add(arbeidsdag)
            }
        }

        override fun postVisitVedtak(
            vedtakId: UUID,
            behandlingId: UUID,
            virkningsdato: LocalDate,
            vedtakstidspunkt: LocalDateTime,
            utfall: Boolean,
        ) {
            if (harDagpengevedtak) {
                this.virkningsdato = virkningsdato
            }
            harDagpengevedtak = false
        }

        override fun visitHelgedag(helgedag: Helgedag) {
            if (helgedag in periode) {
                arbeidsdager.add(helgedag)
            }
        }
    }
}
