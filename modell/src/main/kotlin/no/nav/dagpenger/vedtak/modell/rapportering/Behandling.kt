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
import no.nav.dagpenger.vedtak.modell.hendelser.RapporteringHendelse
import no.nav.dagpenger.vedtak.modell.rapportering.Behandlingsdag.Companion.arbeidedeTimer
import no.nav.dagpenger.vedtak.modell.rapportering.Behandlingsdag.Companion.graderingsProsent
import no.nav.dagpenger.vedtak.modell.rapportering.Behandlingsdag.Companion.tellendeRapporteringsdager
import no.nav.dagpenger.vedtak.modell.rapportering.Behandlingsdag.Companion.vanligArbeidstid
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
    private val behandlingsdager = mutableListOf<Behandlingsdag>()
    private val resultatBuilder: Resultat.Builder = Resultat.Builder()
    fun håndter(rapporteringHendelse: RapporteringHendelse) {
        rapporteringHendelse.kontekst(this)
        behandlingssteg.håndter(rapporteringHendelse, this)
    }

    private fun nesteSteg(behandlingssteg: Behandlingssteg, rapporteringHendelse: RapporteringHendelse) {
        this.behandlingssteg = behandlingssteg
        this.behandlingssteg.entering(rapporteringHendelse, this)
    }

    sealed class Behandlingssteg {
        // peke til hvilken paragraf eller forskrift
        open fun håndter(rapporteringHendelse: RapporteringHendelse, behandling: Behandling) {
            rapporteringHendelse.logiskFeil("Forventet ikke ${rapporteringHendelse.javaClass.simpleName} i ${this.javaClass.simpleName}")
        }

        open fun entering(rapporteringHendelse: RapporteringHendelse, behandling: Behandling) {}
    }

    object FinnBeregningsgrunnlag : Behandlingssteg() {
        override fun håndter(rapporteringHendelse: RapporteringHendelse, behandling: Behandling) {
            behandling.behandlingsdager.addAll(
                BehandlingsdagerForPeriode(
                    rapporteringHendelse,
                    behandling.person,
                ).behandlingsdager,
            )
            behandling.sikkerLogger.info {
                behandling.person.vedtakHistorikk.hovedrettighetHistorikk
            }
            behandling.sikkerLogger.info {
                behandling.behandlingsdager.joinToString("\n") { it.toString() }
            }

            behandling.nesteSteg(VurderTerskelForTaptArbeidstid, rapporteringHendelse)
        }
    }

    object VurderTerskelForTaptArbeidstid : Behandlingssteg() {
        override fun entering(rapporteringHendelse: RapporteringHendelse, behandling: Behandling) {
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
                behandling.nesteSteg(GraderUtbetaling, rapporteringHendelse)
            } else {
                behandling.nesteSteg(Ferdigstill, rapporteringHendelse)
            }
        }
    }

    object GraderUtbetaling : Behandlingssteg() {
        override fun entering(rapporteringHendelse: RapporteringHendelse, behandling: Behandling) {
            val rapporteringsdato = rapporteringHendelse.endInclusive
            val forrigeRapporteringsdato = rapporteringHendelse.start.minusDays(1)
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

            behandling.resultatBuilder.forbruksdager(forbruksdager.map { it.rapporteringsdag })

            val utbetalingsdager =
                forbruksdager.map {
                    it.utbetalingsdag(behandling.behandlingsdager.graderingsProsent())
                }
            behandling.resultatBuilder.utbetalingsdager(utbetalingsdager)

            behandling.nesteSteg(Ferdigstill, rapporteringHendelse)
        }
    }

    object Ferdigstill : Behandlingssteg() {
        override fun entering(rapporteringHendelse: RapporteringHendelse, behandling: Behandling) {
            val resultat = behandling.resultatBuilder.build()
            val utbetalingsvedtak = Utbetalingsvedtak.utbetalingsvedtak(
                behandlingId = behandling.behandlingId,
                sakId = behandling.sakId,
                utfall = resultat.utfall,
                vedtakstidspunkt = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS),
                virkningsdato = rapporteringHendelse.endInclusive,
                forbruk = Stønadsdager(resultat.forbruksdager.size),
                utbetalingsdager = resultat.utbetalingsdager,
            )
            rapporteringHendelse.kontekst(utbetalingsvedtak)
            rapporteringHendelse.info("Fattet utbetalingsvedtak")
            behandling.person.leggTilVedtak(
                utbetalingsvedtak,
            )
        }
    }

    private class Resultat(
        val utfall: Boolean,
        val forbruksdager: List<Rapporteringsdag> = emptyList(),
        val utbetalingsdager: List<Utbetalingsdag> = emptyList(),
    ) {
        class Builder {

            private var utfall: Boolean? = null
            private var forbruksdager: List<Rapporteringsdag> = emptyList()

            private var utbetalingsdager: List<Utbetalingsdag> = emptyList()

            fun utfall(utfall: Boolean) {
                this.utfall = utfall
            }

            fun forbruksdager(forbruksdager: Collection<Rapporteringsdag>) {
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
        private val rapporteringHendelse: RapporteringHendelse,
        private val person: Person,
    ) : PersonVisitor {

        val behandlingsdager = mutableListOf<Behandlingsdag>()

        init {
            person.accept(this)
        }

        override fun visitRapporteringsdag(rapporteringsdag: Rapporteringsdag, aktiviteter: List<Aktivitet>) {
            if (rapporteringsdag.innenfor(rapporteringHendelse)) {
                Behandlingsdag(
                    rapporteringsdag = rapporteringsdag,
                    runCatching { person.vedtakHistorikk.hovedrettighetHistorikk.get(rapporteringsdag.dato()) }.getOrDefault(IngenRettighet),
                    runCatching { person.vedtakHistorikk.vanligArbeidstidHistorikk.get(rapporteringsdag.dato()) }.getOrDefault(0.timer),
                    runCatching { person.vedtakHistorikk.dagsatsHistorikk.get(rapporteringsdag.dato()) }.getOrDefault(0.beløp),
                ).also { behandlingsdager.add(it) }
            }
        }
    }

    override fun toSpesifikkKontekst(): SpesifikkKontekst {
        return SpesifikkKontekst("behandling", mapOf("behandlingId" to behandlingId.toString()))
    }
}
