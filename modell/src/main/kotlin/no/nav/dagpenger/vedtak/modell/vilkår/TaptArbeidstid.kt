package no.nav.dagpenger.vedtak.modell.vilkår

import no.nav.dagpenger.vedtak.modell.Dagpengerettighet
import no.nav.dagpenger.vedtak.modell.Person
import no.nav.dagpenger.vedtak.modell.entitet.Periode
import no.nav.dagpenger.vedtak.modell.entitet.Prosent
import no.nav.dagpenger.vedtak.modell.entitet.Timer
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

internal class TaptArbeidstid(person: Person, val periode: Periode, val tellendeDager: List<Dag>) : PersonVisitor {

    private val arbeidsdager = mutableListOf<Dag>()
    lateinit var virkningsdato: LocalDate
    lateinit var vanligArbeidstidPerDag: Timer
    var harDagpengevedtak = false

    init {
        person.accept(this)
    }

    fun arbeidetUnderTerskel(terskel: Prosent): Boolean {
        val arbeidstimer: Timer = tellendeDager.summer()
        val vanligArbeidstid: Timer = vanligArbeidstidPerDag * tellendeDager.filterIsInstance<Arbeidsdag>().size.toDouble()
        val minsteTapteArbeidstid: Timer = terskel av vanligArbeidstid

        return arbeidstimer <= vanligArbeidstid - minsteTapteArbeidstid
    }

    override fun visitRammeVedtak(
        grunnlag: BigDecimal,
        dagsats: BigDecimal,
        stønadsperiode: Stønadsperiode,
        vanligArbeidstidPerDag: Timer,
        dagpengerettighet: Dagpengerettighet,
    ) {
        this.vanligArbeidstidPerDag = vanligArbeidstidPerDag
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
