import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import no.nav.dagpenger.vedtak.iverksetting.mediator.IverksettingMediator
import no.nav.dagpenger.vedtak.mediator.HendelseMediator
import no.nav.dagpenger.vedtak.mediator.Meldingsfabrikk.dagpengerAvslåttJson
import no.nav.dagpenger.vedtak.mediator.Meldingsfabrikk.dagpengerInnvilgetJson
import no.nav.dagpenger.vedtak.mediator.Meldingsfabrikk.rapporteringInnsendtHendelse
import no.nav.dagpenger.vedtak.mediator.PersonMediator
import no.nav.dagpenger.vedtak.mediator.persistens.InMemoryMeldingRepository
import no.nav.dagpenger.vedtak.mediator.persistens.InMemoryPersonRepository
import no.nav.dagpenger.vedtak.mediator.vedtak.VedtakFattetKafkaObserver
import no.nav.dagpenger.vedtak.modell.PersonObserver
import no.nav.dagpenger.vedtak.modell.vedtak.VedtakObserver
import no.nav.helse.rapids_rivers.asLocalDate
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class PersonMediatorTest {

    private val testRapid = TestRapid()
    private val ident = "11109233444"
    private val testObservatør = TestObservatør()
    private val personRepository = InMemoryPersonRepository()

    init {
        HendelseMediator(
            rapidsConnection = testRapid,
            hendelseRepository = InMemoryMeldingRepository(),
            personMediator = PersonMediator(
                personRepository = personRepository,
                personObservers = listOf(VedtakFattetKafkaObserver(testRapid), testObservatør),
            ),
            iverksettingMediator = IverksettingMediator(mockk(), mockk()),
        )
    }

    @BeforeEach
    fun setUp() {
        testRapid.reset()
        personRepository.reset()
    }

    @Test
    fun `Innvilgelse av dagpenger hendelse fører til vedtak fattet event`() {
        testRapid.sendTestMessage(dagpengerInnvilgetJson(ident = ident))
        testRapid.inspektør.size shouldBe 1
        testRapid.inspektør.message(testRapid.inspektør.size - 1).also {
            assertEquals("vedtak_fattet", it["@event_name"].asText())
        }
        testObservatør.vedtak.shouldNotBeEmpty()
    }

    @Test
    fun `Avslag av dagpenger hendelse fører til vedtak fattet event`() {
        testRapid.sendTestMessage(dagpengerAvslåttJson(ident = ident))
        testRapid.inspektør.size shouldBe 1
        testRapid.inspektør.message(testRapid.inspektør.size - 1).also {
            assertEquals("vedtak_fattet", it["@event_name"].asText())
        }
        testObservatør.vedtak.shouldNotBeEmpty()
    }

    @Test
    fun `Tar imot rapportering behandlet hendelse som fører til vedtak fattet`() {
        testRapid.sendTestMessage(dagpengerInnvilgetJson(ident = ident, virkningsdato = LocalDate.of(2023, 5, 29)))
        testRapid.sendTestMessage(rapporteringInnsendtHendelse(ident = ident))

        testRapid.inspektør.size shouldBe 2

        testRapid.inspektør.message(testRapid.inspektør.size - 1).also {
            it["utbetalingsdager"].map { utbetalingsdagJson ->
                utbetalingsdagJson["dato"].asLocalDate() // TODO: Sjekk noe her?
                utbetalingsdagJson["beløp"].asDouble() shouldBe 0.0
            }
            it["@event_name"].asText() shouldBe "vedtak_fattet"
        }

        testRapid.inspektør.message(testRapid.inspektør.size - 2).also {
            assertEquals("vedtak_fattet", it["@event_name"].asText())
        }
        testObservatør.vedtak.shouldNotBeEmpty()
    }
}

internal class TestObservatør : PersonObserver {

    val vedtak = mutableListOf<VedtakObserver.VedtakFattet>()
    override fun vedtakFattet(ident: String, vedtakFattet: VedtakObserver.VedtakFattet) {
        vedtak.add(vedtakFattet)
    }
}
