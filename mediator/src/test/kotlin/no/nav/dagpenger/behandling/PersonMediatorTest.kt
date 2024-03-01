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
import no.nav.dagpenger.regel.Behov.InntektId
import no.nav.dagpenger.regel.Behov.OpptjeningsperiodeFraOgMed
import no.nav.dagpenger.regel.Behov.SisteAvsluttendeKalenderMåned
import no.nav.helse.rapids_rivers.asLocalDate
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Period

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
                    personobservatører = listOf(KafkaBehandlingObservatør(rapid)),
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
        val testPerson =
            TestPerson(
                ident,
                rapid,
                søknadstidspunkt = 5.mai(2021),
            )
        testPerson.sendSøknad()

        rapid.harBehov("Fødselsdato", "Søknadstidspunkt", "ØnskerDagpengerFraDato")
        testPerson.løsBehov("Fødselsdato", "Søknadstidspunkt", "ØnskerDagpengerFraDato")

        rapid.harBehov(InntektId) {
            medDato(SisteAvsluttendeKalenderMåned) shouldBe 30.april(2021)
            medDato(OpptjeningsperiodeFraOgMed) shouldBe 1.april(2018)

            opptjeningsperiodeEr(måneder = 36)
        }
        testPerson.løsBehov(InntektId)

        rapid.harBehov("InntektSiste12Mnd") { medTekst("InntektId") shouldBe testPerson.inntektId }
        rapid.harBehov("InntektSiste36Mnd") { medTekst("InntektId") shouldBe testPerson.inntektId }

        testPerson.løsBehov("InntektSiste12Mnd", "InntektSiste36Mnd")

        rapid.harHendelse("behandling_avsluttet")
    }

    private fun BehovHelper.opptjeningsperiodeEr(måneder: Int) {
        val periode = Period.between(medDato(OpptjeningsperiodeFraOgMed), medDato(SisteAvsluttendeKalenderMåned))
        withClue("Opptjeningsperiode skal være 3 år") { periode.toTotalMonths() shouldBe måneder }
    }
}

private fun TestRapid.harBehov(vararg behov: String) {
    withClue("Siste melding på rapiden skal inneholde behov: ${behov.toList()}") {
        inspektør.message(inspektør.size - 1)["@behov"].map { it.asText() } shouldContainAll behov.toList()
    }
}

private fun TestRapid.harHendelse(navn: String) {
    withClue("Siste melding på rapiden skal inneholde hendelse: $navn") {
        inspektør.message(inspektør.size - 1)["@event_name"].asText() shouldBe navn
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
    fun medTekst(navn: String) = message.get(navn)?.asText()

    fun medDato(navn: String) = message.get(navn)?.asLocalDate()
}
