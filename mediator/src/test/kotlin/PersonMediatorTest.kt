import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import no.nav.dagpenger.vedtak.iverksetting.mediator.IverksettingMediator
import no.nav.dagpenger.vedtak.mediator.HendelseMediator
import no.nav.dagpenger.vedtak.mediator.Meldingsfabrikk.dagpengerAvslåttJson
import no.nav.dagpenger.vedtak.mediator.Meldingsfabrikk.dagpengerInnvilgetJson
import no.nav.dagpenger.vedtak.mediator.Meldingsfabrikk.rapportering2
import no.nav.dagpenger.vedtak.mediator.PersonMediator
import no.nav.dagpenger.vedtak.mediator.persistens.InMemoryMeldingRepository
import no.nav.dagpenger.vedtak.mediator.persistens.InMemoryPersonRepository
import no.nav.dagpenger.vedtak.mediator.persistens.PersonRepository
import no.nav.dagpenger.vedtak.mediator.vedtak.VedtakFattetKafkaObserver
import no.nav.dagpenger.vedtak.modell.PersonIdentifikator.Companion.tilPersonIdentfikator
import no.nav.dagpenger.vedtak.modell.PersonObserver
import no.nav.dagpenger.vedtak.modell.vedtak.VedtakObserver
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
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

    // fom = LocalDate.of(2023, 5, 1))

    @Test
    fun `Tar imot rapportering behandlet hendelse som fører til vedtak fattet`() {
        testRapid.sendTestMessage(dagpengerInnvilgetJson(ident = ident, virkningsdato = LocalDate.of(2023, 5, 2)))
        testRapid.sendTestMessage(rapportering2(ident = ident))

        testRapid.inspektør.size shouldBe 2
        testRapid.inspektør.message(testRapid.inspektør.size - 1).also {
            assertEquals("vedtak_fattet", it["@event_name"].asText())
        }

        testRapid.inspektør.message(testRapid.inspektør.size - 2).also {
            println(it)
            assertEquals("vedtak_fattet", it["@event_name"].asText())
        }
        testObservatør.vedtak.shouldNotBeEmpty()
    }
}

internal class PersonMediatorKonsistensTest {
    private val testRapid = TestRapid()
    private val testObservatør = TestObservatør()
    private val personRepository = mockk<PersonRepository>()

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

    @Test
    fun `venter til aggregatet er lagret før observere blir kalt`() {
        val feilendeIdent = "23456789101"
        every { personRepository.hent(feilendeIdent.tilPersonIdentfikator()) } returns null
        every { personRepository.lagre(any()) } throws RuntimeException("blaaaa")
        assertThrows<RuntimeException> { testRapid.sendTestMessage(dagpengerInnvilgetJson(ident = feilendeIdent)) }
        testObservatør.vedtak.shouldBeEmpty()
    }
}

internal class TestObservatør : PersonObserver {

    val vedtak = mutableListOf<VedtakObserver.RammevedtakFattet>()
    override fun rammevedtakFattet(ident: String, rammevedtakFattet: VedtakObserver.RammevedtakFattet) {
        vedtak.add(rammevedtakFattet)
    }
}
