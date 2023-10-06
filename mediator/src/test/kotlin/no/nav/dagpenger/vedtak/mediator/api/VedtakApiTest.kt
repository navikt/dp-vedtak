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
import no.nav.dagpenger.vedtak.modell.entitet.Periode
import no.nav.dagpenger.vedtak.modell.entitet.Stønadsdager
import no.nav.dagpenger.vedtak.modell.entitet.Timer.Companion.timer
import no.nav.dagpenger.vedtak.modell.utbetaling.Utbetalingsdag
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

            response.bodyAsText() shouldContain "rammer"
            response.bodyAsText() shouldContain "utbetalinger"
        }
    }

    @Test
    internal fun `200 OK og tom liste av ramme- og utbetalingsvedtak hvis person ikke eksisterer i db`() {
        medSikretVedtakApi {
            val response = autentisert(endepunkt = "/vedtak", body = """{"ident": "$ident"}""")

            print(response.bodyAsText())

            response.status shouldBe HttpStatusCode.OK
            response.contentType().toString() shouldContain "application/json"
            response.bodyAsText() shouldContain "\"rammer\":[]"
            response.bodyAsText() shouldContain "\"utbetalinger\":[]"
        }
    }

    @Test
    internal fun `200 OK og liste med alle rammevedtak for en person`() {
        personRepository.lagre(
            testPersonMed(
                rammevedtak(virkningsdato = LocalDate.parse("2019-08-24")),
            ),
        )

        medSikretVedtakApi {
            val response = autentisert(endepunkt = "/vedtak", body = """{"ident": "$ident"}""")

            println("RES: ${response.bodyAsText()}")
            response.status shouldBe HttpStatusCode.OK
            response.contentType().toString() shouldContain "application/json"
            response.bodyAsText() shouldContain "rammer"
            response.bodyAsText() shouldContain "\"utbetalinger\":[]"
            response.bodyAsText() shouldContain "\"virkningsdato\":\"2019-08-24\""
        }
    }

    @Test
    internal fun `200 OK og list med utbetalingsvedtak for en person `() {
        personRepository.lagre(
            testPersonMed(
                utbetalingsvedtak(
                    virkningsdato = LocalDate.parse("2019-08-24"),
                    utbetalingsdager = listOf(
                        Utbetalingsdag(LocalDate.parse("2019-08-11"), 1000.00.beløp),
                        Utbetalingsdag(LocalDate.parse("2019-08-12"), 1000.00.beløp),
                        Utbetalingsdag(LocalDate.parse("2019-08-13"), 1000.00.beløp),
                        Utbetalingsdag(LocalDate.parse("2019-08-13"), 0.00.beløp),
                        Utbetalingsdag(LocalDate.parse("2019-08-13"), 0.00.beløp),
                    ),

                ),
            ),
        )

        medSikretVedtakApi {
            val response = autentisert(endepunkt = "/vedtak", body = """{"ident": "$ident"}""")

            println("RES: ${response.bodyAsText()}")
            response.status shouldBe HttpStatusCode.OK
            response.contentType().toString() shouldContain "application/json"
            response.bodyAsText() shouldContain "utbetalinger"
            response.bodyAsText() shouldContain "\"rammer\":[]"
            response.bodyAsText() shouldContain "\"fraOgMed\":\"2019-08-11\""
            response.bodyAsText() shouldContain "\"tilOgMed\":\"2019-08-24\""
            response.bodyAsText() shouldContain "\"sumUtbetalt\":3000.0"
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

    private fun utbetalingsvedtak(virkningsdato: LocalDate = LocalDate.MAX, utbetalingsdager: List<Utbetalingsdag> = emptyList()) = Utbetalingsvedtak.utbetalingsvedtak(
        behandlingId = UUID.randomUUID(),
        sakId = "SAK_NUMMER_1",
        utfall = true,
        vedtakstidspunkt = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS),
        virkningsdato = virkningsdato,
        periode = Periode(
            fomDato = virkningsdato.minusDays(13),
            tomDato = virkningsdato,
        ),
        forbruk = Stønadsdager(10),
        utbetalingsdager = utbetalingsdager,
    )

    private fun rammevedtak(virkningsdato: LocalDate = LocalDate.MAX) = Rammevedtak.innvilgelse(
        behandlingId = UUID.randomUUID(),
        sakId = "SAK_NUMMER_1",
        vedtakstidspunkt = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS),
        virkningsdato = virkningsdato,
        dagsats = 1000.beløp,
        stønadsdager = Stønadsdager(104 * 5),
        hovedrettighet = Ordinær(true),
        vanligArbeidstidPerDag = 8.timer,
    )
}
