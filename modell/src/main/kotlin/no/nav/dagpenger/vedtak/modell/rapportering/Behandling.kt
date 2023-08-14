package no.nav.dagpenger.vedtak.modell.rapportering

import no.nav.dagpenger.aktivitetslogg.Aktivitetskontekst
import no.nav.dagpenger.aktivitetslogg.SpesifikkKontekst
import no.nav.dagpenger.vedtak.modell.Dagpengerettighet
import no.nav.dagpenger.vedtak.modell.Person
import no.nav.dagpenger.vedtak.modell.entitet.Prosent
import no.nav.dagpenger.vedtak.modell.entitet.Prosent.Companion.summer
import no.nav.dagpenger.vedtak.modell.entitet.Stønadsdager
import no.nav.dagpenger.vedtak.modell.entitet.Timer
import no.nav.dagpenger.vedtak.modell.entitet.Timer.Companion.summer
import no.nav.dagpenger.vedtak.modell.entitet.Timer.Companion.timer
import no.nav.dagpenger.vedtak.modell.hendelser.Rapporteringshendelse
import no.nav.dagpenger.vedtak.modell.utbetaling.Utbetalingsdag
import no.nav.dagpenger.vedtak.modell.utbetaling.Utbetalingsdag.Companion.summer
import no.nav.dagpenger.vedtak.modell.vedtak.Utbetalingsvedtak
import no.nav.dagpenger.vedtak.modell.visitor.PersonVisitor
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID

class Behandling(
    private val behandlingId: UUID,
    private val sakId: String,
    private val person: Person,
    private var behandlingssteg: Behandlingssteg,
) : Aktivitetskontekst {

    constructor(person: Person, sakId: String) : this(UUID.randomUUID(), sakId, person, FinnBeregningsgrunnlag)

    private val rapporteringsdager = mutableListOf<Dag>()
    private val rettighetsdagerUtenFravær
        get() = rapporteringsdager.filter(dagpengerRettighetsdag()).filterNot(fravær())
    private val tellendeRapporteringsdager get() = rettighetsdagerUtenFravær.filterNot(helgedag())
    private val vanligArbeidstid
        get() = tellendeRapporteringsdager.map {
            vanligArbeidstid(it)
        }.summer()

    private fun vanligArbeidstid(it: Dag) =
        kotlin.runCatching { person.vedtakHistorikk.vanligArbeidstidHistorikk.get(it.dato()) }
            .getOrDefault(0.timer)

    private val arbeidedeTimer get() = rettighetsdagerUtenFravær.map { it.arbeidstimer() }.summer()
    private val taptArbeidstid get() = (vanligArbeidstid - arbeidedeTimer)
    private val graderingsProsent get() = taptArbeidstid prosentAv vanligArbeidstid

    private val resultatBuilder: Resultat.Builder = Resultat.Builder()

    private fun helgedag(): (Dag) -> Boolean = { it.erHelg() }
    private fun fravær(): (Dag) -> Boolean = { it.fravær() }
    private fun dagpengerRettighetsdag(): (Dag) -> Boolean =
        {
            dagpengerettighet(it) != Dagpengerettighet.Ingen
        }

    private fun dagpengerettighet(dag: Dag) =
        kotlin.runCatching { person.vedtakHistorikk.dagpengerettighetHistorikk.get(dag.dato()) }
            .getOrDefault(Dagpengerettighet.Ingen)

    fun håndter(rapporteringshendelse: Rapporteringshendelse) {
        rapporteringshendelse.kontekst(this)
        behandlingssteg.håndter(rapporteringshendelse, this)
    }

    private fun nesteSteg(behandlingssteg: Behandlingssteg, rapporteringshendelse: Rapporteringshendelse) {
        this.behandlingssteg = behandlingssteg
        this.behandlingssteg.entering(rapporteringshendelse, this)
    }

    sealed class Behandlingssteg {
        // peke til hvilken paragraf eller forskrift
        open fun håndter(rapporteringshendelse: Rapporteringshendelse, behandling: Behandling) {
            rapporteringshendelse.logiskFeil("Forventet ikke ${rapporteringshendelse.javaClass.simpleName} i ${this.javaClass.simpleName}")
        }

        open fun entering(rapporteringshendelse: Rapporteringshendelse, behandling: Behandling) {}
    }

    object FinnBeregningsgrunnlag : Behandlingssteg() {
        override fun håndter(rapporteringshendelse: Rapporteringshendelse, behandling: Behandling) {
            behandling.rapporteringsdager.addAll(
                RapporteringsdagerForPeriode(
                    rapporteringshendelse,
                    behandling.person,
                ).dager,
            )

            behandling.nesteSteg(VurderTerskelForTaptArbeidstid, rapporteringshendelse)
        }
    }

    object VurderTerskelForTaptArbeidstid : Behandlingssteg() {
        override fun entering(rapporteringshendelse: Rapporteringshendelse, behandling: Behandling) {
            val arbeidedeTimer = behandling.rettighetsdagerUtenFravær.map { it.arbeidstimer() }.summer()

            val terskelProsent: Prosent = behandling.tellendeRapporteringsdager.map {
                TaptArbeidstidTerskel.terskelFor(behandling.dagpengerettighet(it), it.dato())
            }.summer() / behandling.tellendeRapporteringsdager.size.toDouble()

            val terskelTimer: Timer = terskelProsent av behandling.vanligArbeidstid

            val vilkårOppfylt = arbeidedeTimer <= behandling.vanligArbeidstid - terskelTimer
            behandling.resultatBuilder.utfall(vilkårOppfylt)
            if (vilkårOppfylt) {
                behandling.nesteSteg(GraderUtbetaling, rapporteringshendelse)
            } else {
                behandling.nesteSteg(Ferdigstill, rapporteringshendelse)
            }
        }
    }

    object GraderUtbetaling : Behandlingssteg() {
        override fun entering(rapporteringshendelse: Rapporteringshendelse, behandling: Behandling) {
            val rapporteringsdato = rapporteringshendelse.endInclusive
            val forrigeRapporteringsdato = rapporteringshendelse.start.minusDays(1)
            val forrigeAkkumulerteForbruk =
                behandling.person.vedtakHistorikk.forbrukHistorikk.summer(forrigeRapporteringsdato)
            val innvilgedeStønadsdager = behandling.person.vedtakHistorikk.stønadsdagerHistorikk.get(rapporteringsdato)

            val gjenståendeStønadsperiode = innvilgedeStønadsdager - forrigeAkkumulerteForbruk

            val forbruksdager =
                if (Stønadsdager(dager = behandling.tellendeRapporteringsdager.size) > gjenståendeStønadsperiode) {
                    behandling.tellendeRapporteringsdager.subList(0, gjenståendeStønadsperiode.stønadsdager())
                } else {
                    behandling.tellendeRapporteringsdager
                }

            behandling.resultatBuilder.forbruksdager(forbruksdager)

            val utbetalingsdager =
                forbruksdager.map {
                    Utbetalingsdag(
                        it.dato(),
                        behandling.graderingsProsent * behandling.person.vedtakHistorikk.dagsatsHistorikk.get(it.dato()),
                    )
                }
            behandling.resultatBuilder.utbetalingsdager(utbetalingsdager)

            behandling.nesteSteg(Ferdigstill, rapporteringshendelse)
        }
    }

    object Ferdigstill : Behandlingssteg() {
        override fun entering(rapporteringshendelse: Rapporteringshendelse, behandling: Behandling) {
            val resultat = behandling.resultatBuilder.build()
            val utbetalingsvedtak = Utbetalingsvedtak.utbetalingsvedtak(
                behandlingId = behandling.behandlingId,
                sakId = behandling.sakId,
                utfall = resultat.utfall,
                vedtakstidspunkt = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS),
                virkningsdato = rapporteringshendelse.endInclusive,
                forbruk = Stønadsdager(resultat.forbruksdager.size),
                utbetalingsdager = resultat.utbetalingsdager,
            )
            rapporteringshendelse.kontekst(utbetalingsvedtak)
            rapporteringshendelse.info("Fattet utbetalingsvedtak")
            behandling.person.leggTilVedtak(
                utbetalingsvedtak,
            )
        }
    }

    private class Resultat(
        val utfall: Boolean,
        val forbruksdager: List<Dag> = emptyList(),
        val utbetalingsdager: List<Utbetalingsdag> = emptyList(),
    ) {
        class Builder {

            private var utfall: Boolean? = null
            private var forbruksdager: List<Dag> = emptyList()
            private var utbetalingsdager: List<Utbetalingsdag> = emptyList()

            fun utfall(utfall: Boolean) {
                this.utfall = utfall
            }

            fun forbruksdager(forbruksdager: Collection<Dag>) {
                this.forbruksdager = forbruksdager.toList()
            }

            fun utbetalingsdager(utbetalingsdager: Collection<Utbetalingsdag>) {
                this.utbetalingsdager = utbetalingsdager.toList()
            }

            fun build() = Resultat(
                utfall = requireNotNull(this.utfall) { "Forventer at utfall er satt på resultat" },
                forbruksdager = forbruksdager,
                utbetalingsdager = utbetalingsdager,
            )
        }
    }

    private class RapporteringsdagerForPeriode(private val rapporteringshendelse: Rapporteringshendelse, person: Person) : PersonVisitor {

        val dager = mutableListOf<Dag>()

        init {
            person.accept(this)
        }

        override fun visitDag(dag: Dag, aktiviteter: List<Aktivitet>) {
            if (dag.dato() in rapporteringshendelse) {
                dager.add(dag)
            }
        }
    }

    override fun toSpesifikkKontekst(): SpesifikkKontekst {
        return SpesifikkKontekst("behandling", mapOf("behandlingId" to behandlingId.toString()))
    }
}
