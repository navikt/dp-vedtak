package no.nav.dagpenger.vedtak.cucumber

import com.fasterxml.jackson.annotation.JsonFormat
import io.cucumber.datatable.DataTable
import io.cucumber.java8.No
import no.nav.dagpenger.vedtak.modell.Dagpengerettighet
import no.nav.dagpenger.vedtak.modell.Person
import no.nav.dagpenger.vedtak.modell.PersonIdentifikator.Companion.tilPersonIdentfikator
import no.nav.dagpenger.vedtak.modell.entitet.Beløp
import no.nav.dagpenger.vedtak.modell.entitet.Beløp.Companion.beløp
import no.nav.dagpenger.vedtak.modell.entitet.Timer
import no.nav.dagpenger.vedtak.modell.entitet.Timer.Companion.timer
import no.nav.dagpenger.vedtak.modell.hendelser.DagpengerAvslåttHendelse
import no.nav.dagpenger.vedtak.modell.hendelser.DagpengerInnvilgetHendelse
import no.nav.dagpenger.vedtak.modell.hendelser.Rapporteringsdag
import no.nav.dagpenger.vedtak.modell.hendelser.Rapporteringshendelse
import no.nav.dagpenger.vedtak.modell.hendelser.StansHendelse
import no.nav.dagpenger.vedtak.modell.mengde.Enhet.Companion.arbeidsdager
import no.nav.dagpenger.vedtak.modell.mengde.Enhet.Companion.arbeidsuker
import no.nav.dagpenger.vedtak.modell.mengde.Stønadsperiode
import no.nav.dagpenger.vedtak.modell.mengde.Tid
import no.nav.dagpenger.vedtak.modell.visitor.PersonVisitor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

class RettighetStegTest : No {
    private val datoformatterer = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    private lateinit var person: Person
    private lateinit var ident: String

    private val inspektør get() = Inspektør(person)

    init {
        Gitt("en ny hendelse om innvilget søknad") { søknadHendelse: SøknadInnvilgetHendelseCucumber ->
            ident = søknadHendelse.fødselsnummer
            person = Person(ident.tilPersonIdentfikator())
            person.håndter(
                DagpengerInnvilgetHendelse(
                    ident = ident,
                    behandlingId = UUID.fromString(søknadHendelse.behandlingId),
                    virkningsdato = søknadHendelse.virkningsdato,
                    dagpengerettighet = søknadHendelse.dagpengerettighet,
                    dagsats = søknadHendelse.dagsats.toBigDecimal(),
                    grunnlag = søknadHendelse.grunnlag.toBigDecimal(),
                    stønadsperiode = søknadHendelse.stønadsperiode.arbeidsuker,
                    vanligArbeidstidPerDag = søknadHendelse.vanligArbeidstidPerDag.timer,
                    egenandel = søknadHendelse.egenandel.toBigDecimal(),
                ),
            )
        }
        // TODO: endre type hendelse
        Gitt("et endringsvedtak") { søknadHendelse: SøknadInnvilgetHendelseCucumber ->
            assertPersonOpprettet()
            person.håndter(
                DagpengerInnvilgetHendelse(
                    ident = ident,
                    behandlingId = UUID.fromString(søknadHendelse.behandlingId),
                    virkningsdato = søknadHendelse.virkningsdato,
                    dagpengerettighet = søknadHendelse.dagpengerettighet,
                    dagsats = søknadHendelse.dagsats.toBigDecimal(),
                    grunnlag = søknadHendelse.grunnlag.toBigDecimal(),
                    stønadsperiode = søknadHendelse.stønadsperiode.arbeidsuker,
                    vanligArbeidstidPerDag = søknadHendelse.vanligArbeidstidPerDag.timer,
                    egenandel = søknadHendelse.egenandel.toBigDecimal(),
                ),
            )
        }

        Gitt("en ny hendelse om avslått søknad") { søknadHendelse: SøknadAvslåttHendelseCucumber ->
            ident = søknadHendelse.fødselsnummer
            person = Person(ident.tilPersonIdentfikator())
            person.håndter(
                DagpengerAvslåttHendelse(
                    ident = ident,
                    behandlingId = UUID.fromString(søknadHendelse.behandlingId),
                    virkningsdato = søknadHendelse.virkningsdato,
                ),
            )
        }

        // TODO gjør om til stansHendelse
        Gitt("en ny hendelse om stans") { søknadHendelse: SøknadAvslåttHendelseCucumber ->
            assertPersonOpprettet()
            person.håndter(
                StansHendelse(
                    ident = ident,
                    behandlingId = UUID.fromString(søknadHendelse.behandlingId),
                    virkningsdato = søknadHendelse.virkningsdato,
                ),
            )
        }

        Så("skal bruker ha {int} vedtak") { antallVedtak: Int ->
            assertEquals(antallVedtak, inspektør.antallVedtak)
        }

        Så("vedtaket har dagpengerettighet {string}") { dagpengerettighet: String ->
            assertEquals(Dagpengerettighet.valueOf(dagpengerettighet), inspektør.dagpengerettighet)
        }

        Så("vedtaket har virkningsdato {string}") { virkningsdato: String ->
            assertEquals(LocalDate.parse(virkningsdato, datoformatterer), inspektør.virkningsdato)
        }

        Så("vedtaket har dagsats på {bigdecimal} kroner") { dagsats: BigDecimal ->
            assertEquals(dagsats, inspektør.dagsats)
        }

        Så("vedtaket har grunnlag på {bigdecimal} kroner") { grunnlag: BigDecimal ->
            assertEquals(grunnlag, inspektør.grunnlag)
        }

        Så("vedtaket har stønadsperiode på {int} uker") { stønadsperiode: Int ->
            assertEquals(stønadsperiode.arbeidsuker, inspektør.stønadsperiode)
        }

        Så("vedtaket har vanlig arbeidstid per dag på {double} timer") { vanligArbeidstidPerDag: Double ->
            assertEquals(vanligArbeidstidPerDag.timer, inspektør.vanligArbeidstidPerDag)
        }

        Så("vedtaket har behandlingId lik {string}") { behandlingId: String ->
            assertEquals(UUID.fromString(behandlingId), inspektør.behandlingId)
        }

        Så("skal forbruket være {int} dager") { forbruk: Int ->
            assertEquals(forbruk.arbeidsdager, inspektør.forbruk)
        }

        Så("så skal gjenstående egenandel være {bigdecimal} kr") { egenandel: BigDecimal ->
            assertTrue(inspektør.allEgenandelTrukket) { "Forventet at all egenandel er trukket" }
            assertEquals(Beløp.fra(egenandel), inspektør.gjenståendeEgenandel)
        }

        Så("skal ikke all egenandel være trukket. Gjenstående egenandel er {bigdecimal} kroner") { egenandel: BigDecimal ->
            assertFalse(inspektør.allEgenandelTrukket) { "Forventet ikke at all egenandel er trukket" }
            assertEquals(Beløp.fra(egenandel), inspektør.gjenståendeEgenandel)
        }

        Så("skal utbetalingen være {bigdecimal}") { beløp: BigDecimal ->
            assertEquals(beløp.beløp, inspektør.beløpTilUtbetaling)
        }

        Når("rapporteringshendelse mottas") { rapporteringsHendelse: DataTable ->
            assertPersonOpprettet()
            val rapporteringsdager = rapporteringsHendelse.rows(1).asLists(String::class.java).map {
                Rapporteringsdag(
                    dato = LocalDate.parse(it[0], datoformatterer),
                    fravær = it[1].toBooleanStrict(),
                    timer = it[2].toDouble(),
                )
            }
            håndterRapporteringsHendelse(rapporteringsdager)
        }
    }

    private fun assertPersonOpprettet() {
        assertTrue(this::person.isInitialized) { "Forventer at person er opprettet her" }
    }

    private fun håndterRapporteringsHendelse(rapporteringsdager: List<Rapporteringsdag>) {
        person.håndter(Rapporteringshendelse(ident, UUID.randomUUID(), rapporteringsdager))
    }

    private data class SøknadAvslåttHendelseCucumber(
        val fødselsnummer: String,
        val behandlingId: String,
        val utfall: Boolean,
        @JsonFormat(pattern = "dd.MM.yyyy")
        val virkningsdato: LocalDate,
    )

    private data class SøknadInnvilgetHendelseCucumber(
        val fødselsnummer: String,
        val behandlingId: String,
        val utfall: Boolean,
        @JsonFormat(pattern = "dd.MM.yyyy")
        val virkningsdato: LocalDate,
        val dagpengerettighet: Dagpengerettighet,
        val dagsats: Int,
        val grunnlag: Int,
        val stønadsperiode: Int,
        val vanligArbeidstidPerDag: Double,
        val egenandel: Int,
    )

    private class Inspektør(person: Person) : PersonVisitor {
        init {
            person.accept(this)
        }

        lateinit var gjenståendeEgenandel: Beløp
        lateinit var dagpengerettighet: Dagpengerettighet
        lateinit var behandlingId: UUID
        lateinit var vanligArbeidstidPerDag: Timer
        lateinit var stønadsperiode: Stønadsperiode
        lateinit var grunnlag: BigDecimal
        lateinit var dagsats: BigDecimal
        lateinit var egenandel: BigDecimal
        lateinit var virkningsdato: LocalDate
        lateinit var beløpTilUtbetaling: Beløp
        lateinit var forbruk: Tid
        var antallVedtak = 0
        var allEgenandelTrukket: Boolean = false

        override fun visitGjenståendeEgenandel(gjenståendeEgenandel: Beløp) {
            this.gjenståendeEgenandel = gjenståendeEgenandel
            this.allEgenandelTrukket = this.gjenståendeEgenandel == 0.beløp
        }

        override fun postVisitVedtak(
            vedtakId: UUID,
            behandlingId: UUID,
            virkningsdato: LocalDate,
            vedtakstidspunkt: LocalDateTime,
            utfall: Boolean,
        ) {
            antallVedtak++
            this.virkningsdato = virkningsdato
            this.behandlingId = behandlingId
        }

        override fun visitRammeVedtak(
            grunnlag: BigDecimal,
            dagsats: BigDecimal,
            stønadsperiode: Stønadsperiode,
            vanligArbeidstidPerDag: Timer,
            dagpengerettighet: Dagpengerettighet,
            egenandel: BigDecimal,
        ) {
            this.grunnlag = grunnlag
            this.dagsats = dagsats
            this.stønadsperiode = stønadsperiode
            this.vanligArbeidstidPerDag = vanligArbeidstidPerDag
            this.dagpengerettighet = dagpengerettighet
            this.egenandel = egenandel
        }

        override fun visitLøpendeVedtak(forbruk: Tid, beløpTilUtbetaling: Beløp, trukketEgenandel: Beløp) {
            this.forbruk = forbruk
            this.beløpTilUtbetaling = beløpTilUtbetaling
        }
    }
}
