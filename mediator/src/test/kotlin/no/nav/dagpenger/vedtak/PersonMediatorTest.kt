package no.nav.dagpenger.vedtak

import io.mockk.mockk
import no.nav.dagpenger.vedtak.db.InMemoryMeldingRepository
import no.nav.dagpenger.vedtak.db.InMemoryPersonRepository
import no.nav.dagpenger.vedtak.mediator.BehovMediator
import no.nav.dagpenger.vedtak.mediator.HendelseMediator
import no.nav.dagpenger.vedtak.mediator.PersonMediator
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class PersonMediatorTest {
    private val testRapid = TestRapid()
    private val ident = "11109233444"

    private val personRepository = InMemoryPersonRepository()

    init {
        HendelseMediator(
            rapidsConnection = testRapid,
            personMediator =
                PersonMediator(
                    personRepository = personRepository,
                    aktivitetsloggMediator = mockk(relaxed = true),
                    behovMediator = BehovMediator(testRapid),
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
    fun `e2e av søknad innsendt`() {
        testRapid.sendTestMessage(søknadInnsendtMessage(ident))

        with(testRapid.inspektør) {
            assertEquals(1, size)
            assertEquals(
                listOf(
                    "Fødselsdato",
                    "Søknadsdato",
                    "Siste dag med arbeidsplikt",
                    "Siste dag med lønn",
                    "inntekt12mnd",
                    "inntekt36mnd",
                ),
                field(0, "@behov").map { it.asText() },
            )
        }
    }

    private fun søknadInnsendtMessage(ident: String) =
        JsonMessage.newMessage(
            "innsending_ferdigstilt",
            mapOf(
                "type" to "NySøknad",
                "fødselsnummer" to ident,
                "søknadsData" to
                    mapOf(
                        "søknad_uuid" to "e2e9e3e0-8e3e-4e3e-8e3e-0e3e8e3e9e3e",
                    ),
            ),
        ).toJson()
}
