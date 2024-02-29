package no.nav.dagpenger.behandling

import io.kotest.matchers.collections.shouldContainOnly
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import no.nav.dagpenger.behandling.db.InMemoryMeldingRepository
import no.nav.dagpenger.behandling.db.InMemoryPersonRepository
import no.nav.dagpenger.behandling.mediator.BehovMediator
import no.nav.dagpenger.behandling.mediator.HendelseMediator
import no.nav.dagpenger.behandling.mediator.PersonMediator
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.asLocalDate
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate

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
            size shouldBe 1
            field(0, "@event_name").asText() shouldBe "behov"
            field(0, "@behov").map { it.asText() }.shouldContainOnly(
                "Fødselsdato",
                "Søknadstidspunkt",
                "ØnskerDagpengerFraDato",
            )
        }
        testRapid.sendTestMessage(opplysningSvar1Message(ident))

        with(testRapid.inspektør) {
            size shouldBe 2
            field(1, "@behov").map { it.asText() }.shouldContainOnly(
                "InntektId",
            )
            with(field(1, "InntektId")) {
                this.size() shouldBe 3
                get("Siste avsluttende kalendermåned").asLocalDate() shouldBe LocalDate.of(2021, 4, 30)
                get("Opptjeningsperiode").asLocalDate() shouldBe LocalDate.of(2018, 4, 1)
            }
        }

        testRapid.sendTestMessage(opplysningSvar2Message(ident))
        with(testRapid.inspektør) {
            size shouldBe 3
            field(2, "@behov").map { it.asText() }.shouldContainOnly(
                "InntektSiste12Mnd",
                "InntektSiste36Mnd",
            )
            with(field(2, "InntektSiste12Mnd")) {
                this.size() shouldBe 2
                get("InntektId").asText() shouldBe inntektId
            }
            with(field(2, "InntektSiste36Mnd")) {
                this.size() shouldBe 2
                get("InntektId").asText() shouldBe inntektId
            }
        }

        testRapid.sendTestMessage(opplysningSvar3Message(ident))
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

    private val søknadstidspunkt = LocalDate.of(2021, 5, 5)

    private fun opplysningSvar1Message(ident: String) =
        JsonMessage.newMessage(
            "behov",
            mapOf(
                "ident" to ident,
                "behandlingId" to "e2e9e3e0-8e3e-4e3e-8e3e-0e3e8e3e9e3e",
                "@final" to true,
                "@løsning" to
                    mapOf(
                        "Fødselsdato" to LocalDate.of(1990, 1, 1),
                        "Søknadstidspunkt" to søknadstidspunkt,
                        "ØnskerDagpengerFraDato" to søknadstidspunkt,
                    ),
            ),
        ).toJson()

    private val inntektId = "01HQTE3GBWCSVYH6S436DYFREN"

    private fun opplysningSvar2Message(ident: String) =
        JsonMessage.newMessage(
            "behov",
            mapOf(
                "ident" to ident,
                "behandlingId" to "e2e9e3e0-8e3e-4e3e-8e3e-0e3e8e3e9e3e",
                "@final" to true,
                "@løsning" to
                    mapOf(
                        "InntektId" to inntektId,
                    ),
            ),
        ).toJson()

    private fun opplysningSvar3Message(ident: String) =
        JsonMessage.newMessage(
            "behov",
            mapOf(
                "ident" to ident,
                "behandlingId" to "e2e9e3e0-8e3e-4e3e-8e3e-0e3e8e3e9e3e",
                "@final" to true,
                "@løsning" to
                    mapOf(
                        "InntektSiste12Mnd" to 1234,
                        "InntektSiste36Mnd" to 1234,
                    ),
            ),
        ).toJson()
}
