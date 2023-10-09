package no.nav.dagpenger.vedtak

import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import no.nav.dagpenger.vedtak.db.InMemoryMeldingRepository
import no.nav.dagpenger.vedtak.db.InMemoryPersonRepository
import no.nav.dagpenger.vedtak.mediator.HendelseMediator
import no.nav.dagpenger.vedtak.mediator.Meldingsfabrikk
import no.nav.dagpenger.vedtak.mediator.Meldingsfabrikk.lagRapporteringForMeldeperiodeFørDagpengvedtaket
import no.nav.dagpenger.vedtak.mediator.Meldingsfabrikk.rettighetBehandletOgAvslåttJson
import no.nav.dagpenger.vedtak.mediator.Meldingsfabrikk.rettighetBehandletOgInnvilgetJson
import no.nav.dagpenger.vedtak.mediator.PersonMediator
import no.nav.dagpenger.vedtak.mediator.vedtak.VedtakFattetObserver
import no.nav.dagpenger.vedtak.modell.PersonObserver
import no.nav.dagpenger.vedtak.modell.vedtak.VedtakObserver
import no.nav.helse.rapids_rivers.asLocalDate
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.time.DayOfWeek
import java.time.LocalDate
import kotlin.time.Duration

internal class PersonMediatorTest {

    private val testRapid = TestRapid()
    private val ident = "11109233444"
    private val testObservatør = TestObservatør()
    private val personRepository = InMemoryPersonRepository()

    init {
        HendelseMediator(
            rapidsConnection = testRapid,
            personMediator = PersonMediator(
                aktivitetsloggMediator = mockk(relaxed = true),
                personRepository = personRepository,
                personObservers = listOf(VedtakFattetObserver(testRapid), testObservatør),
            ),
            hendelseRepository = InMemoryMeldingRepository(),
        )
    }

    @BeforeEach
    fun setUp() {
        testRapid.reset()
        personRepository.reset()
    }

    @Test
    fun `Hendelse om innvilgelse av dagpengesøknad fører til dagpenger_innvilget event`() {
        testRapid.sendTestMessage(rettighetBehandletOgInnvilgetJson(ident = ident))
        testRapid.inspektør.size shouldBe 1
        testRapid.inspektør.message(testRapid.inspektør.size - 1).also {
            it["@event_name"].asText() shouldBe "dagpenger_innvilget"
        }
        testObservatør.vedtak.shouldNotBeEmpty()
    }

    @Test
    fun `Hendelse om avslag på dagpengesøknad fører til dagpenger_avslått event`() {
        testRapid.sendTestMessage(rettighetBehandletOgAvslåttJson(ident = ident))
        testRapid.inspektør.size shouldBe 1
        testRapid.inspektør.message(testRapid.inspektør.size - 1).also {
            it["@event_name"].asText() shouldBe "dagpenger_avslått"
        }
        testObservatør.vedtak.shouldNotBeEmpty()
    }

    @Test
    fun `Rapporteringshendelse fører til at utbetalingsvedtak fattes`() {
        val virkningsdatoDagpenger = 29 mai 2023
        innvilgDagpengerFom(virkningsdatoDagpenger)

        testRapid.sendTestMessage(
            Meldingsfabrikk.rapporteringInnsendtJson(
                ident = ident,
                fom = virkningsdatoDagpenger,
                tom = virkningsdatoDagpenger.plusDays(13),
                tidArbeidetPerArbeidsdag = Duration.parseIsoString("PT0S"),
            ),
        )

        testRapid.inspektør.size shouldBe 2
        testObservatør.vedtak.size shouldBe 1

        val rammevedtakJson = testRapid.inspektør.message(testRapid.inspektør.size - 2)
        rammevedtakJson["@event_name"].asText() shouldBe "dagpenger_innvilget"

        testObservatør.utbetalingsvedtak.size shouldBe 1
        val utbetalingsvedtakJson = testRapid.inspektør.message(testRapid.inspektør.size - 1)
        utbetalingsvedtakJson["@event_name"].asText() shouldBe "utbetaling_vedtak_fattet"
        utbetalingsvedtakJson["utbetalingsdager"].size() shouldBe 14
        utbetalingsvedtakJson["utbetalingsdager"].map { utbetalingsdagJson ->
            if (utbetalingsdagJson["dato"].asLocalDate().dayOfWeek !in setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)) {
                utbetalingsdagJson["beløp"].asDouble() shouldBe 1000.0
            }
        }
    }

    @Test
    fun `Hendelse om utbetalingsvedtak fattet skal ha alle rapporteringsdager`() {
        val virkningsdatoDagpenger = 29 mai 2023
        innvilgDagpengerFom(virkningsdatoDagpenger)

        testRapid.sendTestMessage(
            Meldingsfabrikk.rapporteringInnsendtJson(
                ident = ident,
                fom = virkningsdatoDagpenger,
            ),
        )

        testObservatør.utbetalingsvedtak.size shouldBe 1
        val utbetalingsvedtakJson = testRapid.inspektør.message(testRapid.inspektør.size - 1)
        utbetalingsvedtakJson["@event_name"].asText() shouldBe "utbetaling_vedtak_fattet"
        utbetalingsvedtakJson["utbetalingsdager"].size() shouldBe 14
    }

    @Test
    fun `Hendelse om utbetalingsvedtak fattet skal ha alle rapporteringsdager, selv om utfall er avslått`() {
        val virkningsdatoDagpenger = 29 mai 2023
        innvilgDagpengerFom(virkningsdatoDagpenger)

        testRapid.sendTestMessage(
            Meldingsfabrikk.rapporteringInnsendtJson(
                ident = ident,
                fom = virkningsdatoDagpenger,
                tom = virkningsdatoDagpenger.plusDays(13),
                tidArbeidetPerArbeidsdag = Duration.parseIsoString("PT5H"),
            ),
        )

        testObservatør.utbetalingsvedtak.size shouldBe 1
        val utbetalingsvedtakJson = testRapid.inspektør.message(testRapid.inspektør.size - 1)
        utbetalingsvedtakJson["@event_name"].asText() shouldBe "utbetaling_vedtak_fattet"
        utbetalingsvedtakJson["utfall"].asText() shouldBe "Avslått"
        utbetalingsvedtakJson["utbetalingsdager"].size() shouldBe 14
        utbetalingsvedtakJson["utbetalingsdager"].map { utbetalingsdagJson ->
            utbetalingsdagJson["beløp"].asDouble() shouldBe 0.0
        }
    }

    @Test
    @Disabled("Vi må finne hva vi skal gjøre med rapportering hvis bruker ikke har rammevedtak")
    fun `Dersom person har sendt rapportering for en periode uten dagpengevedtak, lager vi ikke utbetalingsvedtak Dette må vi finne ut av`() {
        val virkningsdatoDagpenger = 29 mai 2023
        innvilgDagpengerFom(virkningsdatoDagpenger)

        testRapid.sendTestMessage(
            lagRapporteringForMeldeperiodeFørDagpengvedtaket(ident, virkningsdatoDagpenger),
        )

        testObservatør.vedtak.size shouldBe 1
        val rammevedtakJson = testRapid.inspektør.message(testRapid.inspektør.size - 1)
        rammevedtakJson["@event_name"].asText() shouldBe "vedtak_fattet"
    }

    private fun innvilgDagpengerFom(virkningsdato: LocalDate) {
        testRapid.sendTestMessage(
            rettighetBehandletOgInnvilgetJson(
                ident = ident,
                virkningsdato = virkningsdato,
                dagsats = 1000.0,
                fastsattVanligArbeidstid = 8,
            ),
        )
    }

    private class TestObservatør : PersonObserver {

        val vedtak = mutableListOf<VedtakObserver.VedtakFattet>()
        val utbetalingsvedtak = mutableListOf<VedtakObserver.UtbetalingsvedtakFattet>()

        override fun vedtakFattet(ident: String, vedtakFattet: VedtakObserver.VedtakFattet) {
            vedtak.add(vedtakFattet)
        }

        override fun utbetalingsvedtakFattet(
            ident: String,
            utbetalingsvedtakFattet: VedtakObserver.UtbetalingsvedtakFattet,
        ) {
            utbetalingsvedtak.add(utbetalingsvedtakFattet)
        }
    }
}
