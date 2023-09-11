package no.nav.dagpenger.vedtak.modell.rapportering

import mu.KotlinLogging
import no.nav.dagpenger.aktivitetslogg.Aktivitetskontekst
import no.nav.dagpenger.aktivitetslogg.SpesifikkKontekst
import no.nav.dagpenger.vedtak.modell.Person
import no.nav.dagpenger.vedtak.modell.entitet.Beløp.Companion.beløp
import no.nav.dagpenger.vedtak.modell.entitet.Prosent
import no.nav.dagpenger.vedtak.modell.entitet.Prosent.Companion.summer
import no.nav.dagpenger.vedtak.modell.entitet.Stønadsdager
import no.nav.dagpenger.vedtak.modell.entitet.Timer
import no.nav.dagpenger.vedtak.modell.entitet.Timer.Companion.timer
import no.nav.dagpenger.vedtak.modell.hendelser.Rapporteringshendelse
import no.nav.dagpenger.vedtak.modell.rapportering.Behandlingdag.Companion.arbeidedeTimer
import no.nav.dagpenger.vedtak.modell.rapportering.Behandlingdag.Companion.graderingsProsent
import no.nav.dagpenger.vedtak.modell.rapportering.Behandlingdag.Companion.tellendeRapporteringsdager
import no.nav.dagpenger.vedtak.modell.rapportering.Behandlingdag.Companion.vanligArbeidstid
import no.nav.dagpenger.vedtak.modell.utbetaling.Utbetalingsdag
import no.nav.dagpenger.vedtak.modell.vedtak.Utbetalingsvedtak
import no.nav.dagpenger.vedtak.modell.vedtak.rettighet.IngenRettighet
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

    private val sikkerLogger = KotlinLogging.logger("tjenestekall")
    constructor(person: Person, sakId: String) : this(UUID.randomUUID(), sakId, person, FinnBeregningsgrunnlag)
    private val behandlingsdager = mutableListOf<Behandlingdag>()
    private val resultatBuilder: Resultat.Builder = Resultat.Builder()
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
            behandling.behandlingsdager.addAll(
                BehandlingsdagerForPeriode(
                    rapporteringshendelse,
                    behandling.person,
                ).behandlingsdager,
            )
            behandling.sikkerLogger.info {
                behandling.person.vedtakHistorikk.hovedrettighetHistorikk
            }
            behandling.sikkerLogger.info {
                behandling.behandlingsdager.joinToString("\n") { it.toString() }
            }

            behandling.nesteSteg(VurderTerskelForTaptArbeidstid, rapporteringshendelse)
        }
    }

    object VurderTerskelForTaptArbeidstid : Behandlingssteg() {
        override fun entering(rapporteringshendelse: Rapporteringshendelse, behandling: Behandling) {
            val arbeidedeTimer = behandling.behandlingsdager.arbeidedeTimer()

            val terskel = behandling.behandlingsdager.tellendeRapporteringsdager().map {
                it.terskel()
            }.summer()
            behandling.sikkerLogger.info {
                "Terskel for tapt arbeidstid: $terskel\n" +
                    "Tellende rapporteringsdager : ${behandling.behandlingsdager.tellendeRapporteringsdager().joinToString("\n") { it.toString() }}"
            }
            val terskelProsent: Prosent = terskel / behandling.behandlingsdager.tellendeRapporteringsdager().size.toDouble()

            val terskelTimer: Timer = terskelProsent av behandling.behandlingsdager.vanligArbeidstid()

            val vilkårOppfylt = arbeidedeTimer <= behandling.behandlingsdager.vanligArbeidstid() - terskelTimer
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
                if (Stønadsdager(dager = behandling.behandlingsdager.tellendeRapporteringsdager().size) > gjenståendeStønadsperiode) {
                    behandling.behandlingsdager.tellendeRapporteringsdager().subList(0, gjenståendeStønadsperiode.stønadsdager())
                } else {
                    behandling.behandlingsdager.tellendeRapporteringsdager()
                }

            behandling.resultatBuilder.forbruksdager(forbruksdager.map { it.dag })

            val utbetalingsdager =
                forbruksdager.map {
                    it.utbetalingsdag(behandling.behandlingsdager.graderingsProsent())
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

    private class BehandlingsdagerForPeriode(
        private val rapporteringshendelse: Rapporteringshendelse,
        private val person: Person,
    ) : PersonVisitor {

        val behandlingsdager = mutableListOf<Behandlingdag>()

        init {
            person.accept(this)
        }

        override fun visitDag(dag: Dag, aktiviteter: List<Aktivitet>) {
            if (dag.innenfor(rapporteringshendelse)) {
                Behandlingdag(
                    dag = dag,
                    runCatching { person.vedtakHistorikk.hovedrettighetHistorikk.get(dag.dato()) }.getOrDefault(IngenRettighet),
                    runCatching { person.vedtakHistorikk.vanligArbeidstidHistorikk.get(dag.dato()) }.getOrDefault(0.timer),
                    runCatching { person.vedtakHistorikk.dagsatsHistorikk.get(dag.dato()) }.getOrDefault(0.beløp),
                ).also { behandlingsdager.add(it) }
            }
        }
    }

    override fun toSpesifikkKontekst(): SpesifikkKontekst {
        return SpesifikkKontekst("behandling", mapOf("behandlingId" to behandlingId.toString()))
    }
}
