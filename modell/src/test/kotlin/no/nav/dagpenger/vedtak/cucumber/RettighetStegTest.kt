package no.nav.dagpenger.vedtak.cucumber

import com.fasterxml.jackson.annotation.JsonFormat
import io.cucumber.datatable.DataTable
import io.cucumber.java8.No
import no.nav.dagpenger.vedtak.modell.Person
import no.nav.dagpenger.vedtak.modell.PersonIdentifikator.Companion.tilPersonIdentfikator
import no.nav.dagpenger.vedtak.modell.SakId
import no.nav.dagpenger.vedtak.modell.entitet.Beløp
import no.nav.dagpenger.vedtak.modell.entitet.Beløp.Companion.beløp
import no.nav.dagpenger.vedtak.modell.entitet.Dagpengeperiode
import no.nav.dagpenger.vedtak.modell.entitet.Periode
import no.nav.dagpenger.vedtak.modell.entitet.Stønadsdager
import no.nav.dagpenger.vedtak.modell.entitet.Timer
import no.nav.dagpenger.vedtak.modell.entitet.Timer.Companion.timer
import no.nav.dagpenger.vedtak.modell.hendelser.RapporteringHendelse
import no.nav.dagpenger.vedtak.modell.hendelser.RapporteringshendelseDag
import no.nav.dagpenger.vedtak.modell.hendelser.RettighetBehandletOgAvslåttHendelse
import no.nav.dagpenger.vedtak.modell.hendelser.RettighetBehandletOgInnvilgetHendelse
import no.nav.dagpenger.vedtak.modell.hendelser.StansHendelse
import no.nav.dagpenger.vedtak.modell.utbetaling.Utbetalingsdag
import no.nav.dagpenger.vedtak.modell.vedtak.Vedtak
import no.nav.dagpenger.vedtak.modell.vedtak.rettighet.Ordinær
import no.nav.dagpenger.vedtak.modell.vedtak.rettighet.Permittering
import no.nav.dagpenger.vedtak.modell.vedtak.rettighet.PermitteringFraFiskeindustrien
import no.nav.dagpenger.vedtak.modell.vedtak.rettighet.Rettighet
import no.nav.dagpenger.vedtak.modell.visitor.PersonVisitor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.UUID
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

class RettighetStegTest : No {
    private val datoformatterer = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    private lateinit var person: Person
    private lateinit var ident: String

    private val inspektør get() = Inspektør(person)

    init {
        Gitt("en ny hendelse om innvilget søknad") { søknadHendelse: RettighetInnvilgetHendelseCucumber ->
            ident = søknadHendelse.fødselsnummer
            person = Person(ident.tilPersonIdentfikator())
            person.håndter(
                RettighetBehandletOgInnvilgetHendelse(
                    meldingsreferanseId = UUID.randomUUID(),
                    sakId = søknadHendelse.sakId,
                    ident = ident,
                    behandlingId = UUID.fromString(søknadHendelse.behandlingId),
                    vedtakstidspunkt = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS),
                    virkningsdato = søknadHendelse.virkningsdato,
                    hovedrettighet =
                        when (søknadHendelse.dagpengerettighet) {
                            "Ordinær" -> Ordinær(true)
                            "Permittering" -> Permittering(true)
                            "PermitteringFraFiskeindustrien" -> PermitteringFraFiskeindustrien(true)
                            else -> throw IllegalArgumentException("Hvilken rettighet skal $this mappes til?")
                        },
                    dagsats = søknadHendelse.dagsats.beløp,
                    stønadsdager = Dagpengeperiode(søknadHendelse.stønadsperiode).tilStønadsdager(),
                    vanligArbeidstidPerDag = søknadHendelse.vanligArbeidstidPerDag.timer,
                ),
            )
        }
        // TODO: endre type hendelse
        Gitt("et endringsvedtak") { søknadHendelse: RettighetInnvilgetHendelseCucumber ->
            assertPersonOpprettet()
            person.håndter(
                RettighetBehandletOgInnvilgetHendelse(
                    meldingsreferanseId = UUID.randomUUID(),
                    ident = ident,
                    sakId = søknadHendelse.sakId,
                    behandlingId = UUID.fromString(søknadHendelse.behandlingId),
                    vedtakstidspunkt = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS),
                    virkningsdato = søknadHendelse.virkningsdato,
                    hovedrettighet = hovedrettighet(hovedrettighet = søknadHendelse.dagpengerettighet, utfall = true),
                    dagsats = søknadHendelse.dagsats.beløp,
                    stønadsdager = Dagpengeperiode(antallUker = søknadHendelse.stønadsperiode).tilStønadsdager(),
                    vanligArbeidstidPerDag = søknadHendelse.vanligArbeidstidPerDag.timer,
                ),
            )
        }

        Gitt("en ny hendelse om avslått søknad") { søknadHendelse: RettighetAvslåttHendelseCucumber ->
            ident = søknadHendelse.fødselsnummer
            person = Person(ident.tilPersonIdentfikator())
            person.håndter(
                RettighetBehandletOgAvslåttHendelse(
                    meldingsreferanseId = UUID.randomUUID(),
                    sakId = søknadHendelse.sakId,
                    ident = ident,
                    behandlingId = UUID.fromString(søknadHendelse.behandlingId),
                    vedtakstidspunkt = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS),
                    virkningsdato = søknadHendelse.virkningsdato,
                    hovedrettighet = hovedrettighet(hovedrettighet = søknadHendelse.dagpengerettighet, utfall = false),
                ),
            )
        }

        // TODO gjør om til stansHendelse
        Gitt("en ny hendelse om stans") { søknadHendelse: RettighetAvslåttHendelseCucumber ->
            assertPersonOpprettet()
            person.håndter(
                StansHendelse(
                    meldingsreferanseId = UUID.randomUUID(),
                    sakId = søknadHendelse.sakId,
                    ident = ident,
                    behandlingId = UUID.fromString(søknadHendelse.behandlingId),
                    vedtakstidspunkt = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS),
                    virkningsdato = søknadHendelse.virkningsdato,
                ),
            )
        }

        Så("skal bruker ha {int} vedtak") { antallVedtak: Int ->
            assertEquals(antallVedtak, inspektør.antallVedtak)
        }

        Så("vedtaket har dagpengerettighet {string}") { dagpengerettighet: String ->
            assertEquals(Rettighet.RettighetType.valueOf(dagpengerettighet), inspektør.dagpengerettighet)
        }

        Så("vedtaket har virkningsdato {string}") { virkningsdato: String ->
            assertEquals(LocalDate.parse(virkningsdato, datoformatterer), inspektør.virkningsdato)
        }

        Så("vedtaket har dagsats på {bigdecimal} kroner") { dagsats: BigDecimal ->
            assertEquals(dagsats.beløp, inspektør.dagsats)
        }

        Så("vedtaket har stønadsperiode på {int} uker") { stønadsperiode: Int ->
            assertEquals(Dagpengeperiode(stønadsperiode).tilStønadsdager(), inspektør.stønadsdager)
        }

        Så("vedtaket har vanlig arbeidstid per dag på {double} timer") { vanligArbeidstidPerDag: Double ->
            assertEquals(vanligArbeidstidPerDag.timer, inspektør.vanligArbeidstidPerDag)
        }

        Så("vedtaket har behandlingId lik {string}") { behandlingId: String ->
            assertEquals(UUID.fromString(behandlingId), inspektør.behandlingId)
        }

        Så("skal utfall være {string}") { utfall: String ->
            assertEquals(utfall, inspektør.utfall.toString())
        }

        Så("skal forbruket være {int} dager") { forbruk: Int ->
            assertEquals(Stønadsdager(dager = forbruk), inspektør.forbruk)
        }

        Så("skal utbetalingen være {bigdecimal}") { beløp: BigDecimal ->
            assertEquals(beløp.beløp, inspektør.beløpTilUtbetaling)
        }

        Så("skal beregnet utbetaling være {bigdecimal} kr for {string}") { beløp: BigDecimal, virkningsdato: String ->
            val beløpTilUtbetaling = person.beløpTilUtbetalingFor(LocalDate.parse(virkningsdato, datoformatterer))
            assertEquals(beløp.beløp, beløpTilUtbetaling)
        }

        Så("skal gjenstående stønadsdager være {int} fra {string}") { dager: Int, virkningsdato: String ->
            val gjenståendeStønadsdager =
                person.gjenståendeStønadsdagerFra(LocalDate.parse(virkningsdato, datoformatterer))
            assertEquals(Stønadsdager(dager = dager), gjenståendeStønadsdager)
        }

        Når("rapporteringshendelse mottas") { rapporteringHendelse: DataTable ->
            assertPersonOpprettet()
            val rapporteringsdager =
                rapporteringHendelse.rows(1).asLists(String::class.java).map {
                    RapporteringshendelseDag(
                        dato = LocalDate.parse(it[0], datoformatterer),
                        aktiviteter =
                            listOf(
                                lagAktivitet(it),
                            ),
                    )
                }
            håndterRapporteringHendelse(rapporteringsdager)
        }
    }

    private fun hovedrettighet(
        hovedrettighet: String,
        utfall: Boolean,
    ) = when (hovedrettighet) {
        "Ordinær" -> Ordinær(utfall)
        else -> throw IllegalArgumentException("Kjenner ikke rettighet $hovedrettighet")
    }

    private fun lagAktivitet(data: MutableList<String>) =
        when (data[1].toBooleanStrict()) {
            true -> RapporteringshendelseDag.Aktivitet(RapporteringshendelseDag.Aktivitet.Type.Syk, 1.days)
            false ->
                RapporteringshendelseDag.Aktivitet(
                    RapporteringshendelseDag.Aktivitet.Type.Arbeid,
                    data[2].toDouble().hours,
                )
        }

    private fun assertPersonOpprettet() {
        assertTrue(this::person.isInitialized) { "Forventer at person er opprettet her" }
    }

    private fun håndterRapporteringHendelse(rapporteringsdager: List<RapporteringshendelseDag>) {
        person.håndter(
            RapporteringHendelse(
                meldingsreferanseId = UUID.randomUUID(),
                ident = ident,
                rapporteringsId = UUID.randomUUID(),
                rapporteringsdager = rapporteringsdager,
                fom = rapporteringsdager.minOf { it.dato },
                tom = rapporteringsdager.maxOf { it.dato },
            ),
        )
    }

    private data class RettighetAvslåttHendelseCucumber(
        val sakId: SakId = "SAK_NUMMER_1",
        val fødselsnummer: String,
        val behandlingId: String,
        val dagpengerettighet: String,
        val utfall: String,
        @JsonFormat(pattern = "dd.MM.yyyy")
        val virkningsdato: LocalDate,
    )

    private data class RettighetInnvilgetHendelseCucumber(
        val sakId: SakId = "SAK_NUMMER_1",
        val fødselsnummer: String,
        val behandlingId: String,
        val utfall: String,
        @JsonFormat(pattern = "dd.MM.yyyy")
        val virkningsdato: LocalDate,
        val dagpengerettighet: String,
        val dagsats: Int,
        val stønadsperiode: Int,
        val vanligArbeidstidPerDag: Double,
    )

    private class Inspektør(person: Person) : PersonVisitor {
        init {
            person.accept(this)
        }

        private var vedtakId: UUID? = null
        lateinit var sakId: SakId
        lateinit var dagpengerettighet: Rettighet.RettighetType
        lateinit var behandlingId: UUID
        lateinit var vanligArbeidstidPerDag: Timer
        lateinit var stønadsdager: Stønadsdager
        lateinit var dagsats: Beløp
        lateinit var virkningsdato: LocalDate
        lateinit var beløpTilUtbetaling: Beløp
        lateinit var forbruk: Stønadsdager
        var utfall = false
        var antallVedtak = 0

        override fun preVisitVedtak(
            vedtakId: UUID,
            sakId: SakId,
            behandlingId: UUID,
            virkningsdato: LocalDate,
            vedtakstidspunkt: LocalDateTime,
            type: Vedtak.VedtakType,
        ) {
            this.vedtakId = vedtakId
            this.sakId = sakId
            this.virkningsdato = virkningsdato
            this.behandlingId = behandlingId
            antallVedtak++
        }

        override fun visitOrdinær(ordinær: Ordinær) {
            this.dagpengerettighet = ordinær.type
        }

        override fun visitPermitteringFraFiskeindustrien(permitteringFraFiskeindustrien: PermitteringFraFiskeindustrien) {
            this.dagpengerettighet = permitteringFraFiskeindustrien.type
        }

        override fun visitPermittering(permittering: Permittering) {
            this.dagpengerettighet = permittering.type
        }

        override fun visitAntallStønadsdager(dager: Stønadsdager) {
            this.stønadsdager = dager
        }

        override fun visitVanligArbeidstidPerDag(timer: Timer) {
            this.vanligArbeidstidPerDag = timer
        }

        override fun visitDagsats(beløp: Beløp) {
            this.dagsats = beløp
        }

        override fun postVisitVedtak(
            vedtakId: UUID,
            sakId: SakId,
            behandlingId: UUID,
            virkningsdato: LocalDate,
            vedtakstidspunkt: LocalDateTime,
            type: Vedtak.VedtakType,
        ) {
            this.vedtakId = null
        }

        override fun visitUtbetalingsvedtak(
            vedtakId: UUID,
            periode: Periode,
            utfall: Boolean,
            forbruk: Stønadsdager,
            beløpTilUtbetaling: Beløp,
            utbetalingsdager: List<Utbetalingsdag>,
        ) {
            this.utfall = utfall
            this.forbruk = forbruk
            this.beløpTilUtbetaling = beløpTilUtbetaling
            this.virkningsdato = virkningsdato
            this.behandlingId = behandlingId
        }

        override fun visitStans(
            vedtakId: UUID,
            sakId: SakId,
            behandlingId: UUID,
            virkningsdato: LocalDate,
            vedtakstidspunkt: LocalDateTime,
            utfall: Boolean?,
        ) {
            antallVedtak++
            this.virkningsdato = virkningsdato
            this.behandlingId = behandlingId
        }

        override fun visitAvslag(
            vedtakId: UUID,
            sakId: SakId,
            behandlingId: UUID,
            vedtakstidspunkt: LocalDateTime,
            utfall: Boolean,
            virkningsdato: LocalDate,
        ) {
            antallVedtak++
            this.virkningsdato = virkningsdato
            this.behandlingId = behandlingId
        }
    }
}
