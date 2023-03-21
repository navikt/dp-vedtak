package no.nav.dagpenger.vedtak.modell.rapportering

import no.nav.dagpenger.vedtak.modell.Dagpengerettighet
import no.nav.dagpenger.vedtak.modell.Person
import no.nav.dagpenger.vedtak.modell.entitet.Periode
import no.nav.dagpenger.vedtak.modell.entitet.Timer
import no.nav.dagpenger.vedtak.modell.mengde.Stønadsperiode
import no.nav.dagpenger.vedtak.modell.visitor.PersonVisitor
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

internal class TellendeDager(person: Person, val periode: Periode) : PersonVisitor {

    private val dager = mutableListOf<Dag>()
    lateinit var virkningsdato: LocalDate
    var harDagpengevedtak = false

    init {
        person.accept(this)
    }

    fun tellendeDager() = dager.filter { dato -> dato >= virkningsdato }

    override fun visitArbeidsdag(arbeidsdag: Arbeidsdag) {
        if (arbeidsdag in periode) {
            dager.add(arbeidsdag)
        }
    }

    override fun visitRammeVedtak(
        grunnlag: BigDecimal,
        dagsats: BigDecimal,
        stønadsperiode: Stønadsperiode,
        fastsattArbeidstidPerDag: Timer,
        dagpengerettighet: Dagpengerettighet,
    ) {
        harDagpengevedtak = true
    }

    override fun postVisitVedtak(
        vedtakId: UUID,
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
            dager.add(helgedag)
        }
    }
}
