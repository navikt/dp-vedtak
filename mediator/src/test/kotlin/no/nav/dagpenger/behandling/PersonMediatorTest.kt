package no.nav.dagpenger.behandling

import com.fasterxml.jackson.databind.JsonNode
import io.kotest.assertions.withClue
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import no.nav.dagpenger.behandling.db.InMemoryMeldingRepository
import no.nav.dagpenger.behandling.db.InMemoryPersonRepository
import no.nav.dagpenger.behandling.mediator.BehovMediator
import no.nav.dagpenger.behandling.mediator.HendelseMediator
import no.nav.dagpenger.behandling.mediator.PersonMediator
import no.nav.helse.rapids_rivers.asLocalDate
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class PersonMediatorTest {
    private val rapid = TestRapid()
    private val ident = "11109233444"

    private val personRepository = InMemoryPersonRepository()

    init {
        HendelseMediator(
            rapidsConnection = rapid,
            personMediator =
                PersonMediator(
                    personRepository = personRepository,
                    aktivitetsloggMediator = mockk(relaxed = true),
                    behovMediator = BehovMediator(rapid),
                ),
            hendelseRepository = InMemoryMeldingRepository(),
        )
    }

    @BeforeEach
    fun setUp() {
        rapid.reset()
        personRepository.reset()
    }

    @Test
    fun `e2e av søknad innsendt`() {
        val testCase = TestCase(ident, rapid)
        testCase.sendSøknad()

        rapid.harBehov("Fødselsdato", "Søknadstidspunkt", "ØnskerDagpengerFraDato")
        testCase.løsBehov("Fødseldato", "Søknadstidspunkt", "ØnskerDagpengerFraDato")

        rapid.harBehov("InntektId") {
            dato("Siste avsluttende kalendermåned") shouldBe LocalDate.of(2021, 4, 30)
            dato("Opptjeningsperiode") shouldBe LocalDate.of(2018, 4, 1)
        }
        testCase.løsBehov("InntektId")

        rapid.harBehov("InntektSiste12Mnd") { tekst("InntektId") shouldBe testCase.inntektId }
        rapid.harBehov("InntektSiste36Mnd") { tekst("InntektId") shouldBe testCase.inntektId }

        testCase.løsBehov("InntektSiste12Mnd", "InntektSiste36Mnd")
    }
}

private fun TestRapid.harBehov(vararg behov: String) {
    withClue("Siste melding på rapiden skal inneholde behov: ${behov.toList()}") {
        inspektør.message(inspektør.size - 1)["@behov"].map { it.asText() } shouldContainAll behov.toList()
    }
}

private fun TestRapid.harBehov(
    behov: String,
    block: BehovHelper.() -> Unit,
) {
    harBehov(behov)
    BehovHelper(inspektør.message(inspektør.size - 1)[behov]).apply { block() }
}

private class BehovHelper(private val message: JsonNode) {
    fun tekst(navn: String) = message.get(navn)?.asText()

    fun dato(navn: String) = message.get(navn)?.asLocalDate()
}
