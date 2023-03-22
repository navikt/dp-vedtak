package no.nav.dagpenger.vedtak.modell.vilkår

import no.nav.dagpenger.vedtak.modell.Dagpengerettighet
import no.nav.dagpenger.vedtak.modell.Person
import no.nav.dagpenger.vedtak.modell.entitet.Periode
import no.nav.dagpenger.vedtak.modell.entitet.Prosent
import no.nav.dagpenger.vedtak.modell.entitet.Timer
import no.nav.dagpenger.vedtak.modell.hendelser.Rapporteringshendelse
import no.nav.dagpenger.vedtak.modell.mengde.Stønadsperiode
import no.nav.dagpenger.vedtak.modell.rapportering.Dag
import no.nav.dagpenger.vedtak.modell.vilkår.LøpendeStønadsperiodeVilkår.IkkeOppfylt
import no.nav.dagpenger.vedtak.modell.vilkår.LøpendeStønadsperiodeVilkår.Oppfylt
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
            val dagpengerettighet = GjeldendeDagpengerettighet(vilkårsvurdering.person).dagpengerettighet()
            val arbeidetUnderTerskel = TaptArbeidstid(
                vilkårsvurdering.person,
                rapporteringsHendelse.somPeriode(),
                tellendeDager,
            ).arbeidetUnderTerskel(
                finnTerskel(dagpengerettighet),
            )
            if (harGjenstående && arbeidetUnderTerskel) {
                vilkårsvurdering.endreTilstand(nyTilstand = Oppfylt)
            } else {
                vilkårsvurdering.endreTilstand(nyTilstand = IkkeOppfylt)
            }
        }

        private fun finnTerskel(dagpengerettighet: Dagpengerettighet): Prosent {
            val vanligTerskel = Prosent(50.0)
            val fiskeTerskel = Prosent(40.0)
            return if (dagpengerettighet != Dagpengerettighet.PermitteringFraFiskeindustrien) vanligTerskel else fiskeTerskel
        }
    }

    object Oppfylt : Tilstand.Oppfylt<LøpendeStønadsperiodeVilkår>()

    object IkkeOppfylt : Tilstand.IkkeOppfylt<LøpendeStønadsperiodeVilkår>()

    override fun <T> implementasjon(block: LøpendeStønadsperiodeVilkår.() -> T): T {
        return this.block()
    }

    private class GjeldendeDagpengerettighet(person: Person) : PersonVisitor {
        init {
            person.accept(this)
        }

        private lateinit var dagpengerettighet: Dagpengerettighet

        fun dagpengerettighet() = dagpengerettighet

        override fun visitRammeVedtak(
            grunnlag: BigDecimal,
            dagsats: BigDecimal,
            stønadsperiode: Stønadsperiode,
            vanligArbeidstidPerDag: Timer,
            dagpengerettighet: Dagpengerettighet,
        ) {
            this.dagpengerettighet = dagpengerettighet
        }
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
}
