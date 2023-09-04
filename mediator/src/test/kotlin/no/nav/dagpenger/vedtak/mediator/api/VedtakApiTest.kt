package no.nav.dagpenger.vedtak.mediator.api

import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType.Application.Json
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.ApplicationTestBuilder
import no.nav.dagpenger.vedtak.db.InMemoryPersonRepository
import no.nav.dagpenger.vedtak.mediator.api.TestApplication.autentisert
import no.nav.dagpenger.vedtak.mediator.api.TestApplication.testAzureAdToken
import no.nav.dagpenger.vedtak.mediator.persistens.PersonRepository
import no.nav.dagpenger.vedtak.modell.Person
import no.nav.dagpenger.vedtak.modell.PersonIdentifikator.Companion.tilPersonIdentfikator
import no.nav.dagpenger.vedtak.modell.entitet.Beløp.Companion.beløp
import no.nav.dagpenger.vedtak.modell.entitet.Stønadsdager
import no.nav.dagpenger.vedtak.modell.entitet.Timer.Companion.timer
import no.nav.dagpenger.vedtak.modell.vedtak.Rammevedtak
import no.nav.dagpenger.vedtak.modell.vedtak.Utbetalingsvedtak
import no.nav.dagpenger.vedtak.modell.vedtak.Vedtak
import no.nav.dagpenger.vedtak.modell.vedtak.rettighet.Ordinær
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID

class VedtakApiTest {

    private val ident = "12345123451"
    private val personRepository = InMemoryPersonRepository()

    @AfterEach
    fun tearDown() {
        personRepository.reset()
    }

    @Test
    fun `ikke autentiserte kall returnerer 401`() {
        medSikretVedtakApi {
            val response = client.post("/vedtak") {
                contentType(Json)
                setBody("""{"ident": "$ident"}""")
            }
            response.status shouldBe HttpStatusCode.Unauthorized
        }
    }

    @Test
    fun `kall uten saksbehandlingsADgruppe i claims returnerer 401`() {
        medSikretVedtakApi {
            val tokenUtenSaksbehandlerGruppe = testAzureAdToken(ADGrupper = emptyList())

            val response = autentisert(
                token = tokenUtenSaksbehandlerGruppe,
                endepunkt = "/vedtak",
                body = """{"ident": "$ident"}""",
            )
            response.status shouldBe HttpStatusCode.Unauthorized
        }
    }

    @Test
    fun `200 OK og liste med alle vedtak for en person`() {
        personRepository.lagre(
            testPersonMed(
                rammevedtak(),
                utbetalingsvedtak(),
            ),
        )

        medSikretVedtakApi {
            val response = autentisert(endepunkt = "/vedtak", body = """{"ident": "$ident"}""")

            response.status shouldBe HttpStatusCode.OK
            response.contentType().toString() shouldContain "application/json"
            response.bodyAsText() shouldContain "Ramme"
            response.bodyAsText() shouldContain "Utbetaling"
        }
    }

    @Test
    internal fun `200 OK og tom liste hvis person ikke eksisterer i db`() {
        medSikretVedtakApi {
            val response = autentisert(endepunkt = "/vedtak", body = """{"ident": "$ident"}""")

            response.status shouldBe HttpStatusCode.OK
            response.contentType().toString() shouldContain "application/json"
            response.bodyAsText() shouldBe "[]"
        }
    }

    private fun medSikretVedtakApi(
        personRepository: PersonRepository = this.personRepository,
        test: suspend ApplicationTestBuilder.() -> Unit,
    ) {
        TestApplication.withMockAuthServerAndTestApplication(
            moduleFunction = {
                vedtakApi(personRepository)
            },
            test,
        )
    }

    private fun testPersonMed(vararg vedtak: Vedtak) = Person.rehydrer(
        ident = "12345123451".tilPersonIdentfikator(),
        saker = mutableListOf(),
        vedtak = vedtak.toList(),
        perioder = emptyList(),
    )

    private fun utbetalingsvedtak() = Utbetalingsvedtak.utbetalingsvedtak(
        behandlingId = UUID.randomUUID(),
        sakId = "SAK_NUMMER_1",
        utfall = true,
        vedtakstidspunkt = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS),
        virkningsdato = LocalDate.MAX,
        forbruk = Stønadsdager(10),
        utbetalingsdager = emptyList(),
    )

    private fun rammevedtak() = Rammevedtak.innvilgelse(
        behandlingId = UUID.randomUUID(),
        sakId = "SAK_NUMMER_1",
        vedtakstidspunkt = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS),
        virkningsdato = LocalDate.MAX,
        dagsats = 1000.beløp,
        stønadsdager = Stønadsdager(104 * 5),
        hovedrettighet = Ordinær(true),
        vanligArbeidstidPerDag = 8.timer,
    )
}
