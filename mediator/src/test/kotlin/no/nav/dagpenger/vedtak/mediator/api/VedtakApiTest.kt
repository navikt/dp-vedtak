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
import io.ktor.server.testing.testApplication
import no.nav.dagpenger.aktivitetslogg.Aktivitetslogg
import no.nav.dagpenger.vedtak.mediator.persistens.InMemoryPersonRepository
import no.nav.dagpenger.vedtak.mediator.persistens.PersonRepository
import no.nav.dagpenger.vedtak.modell.Dagpengerettighet
import no.nav.dagpenger.vedtak.modell.Person
import no.nav.dagpenger.vedtak.modell.PersonIdentifikator.Companion.tilPersonIdentfikator
import no.nav.dagpenger.vedtak.modell.entitet.Beløp.Companion.beløp
import no.nav.dagpenger.vedtak.modell.entitet.Stønadsdager
import no.nav.dagpenger.vedtak.modell.entitet.Timer.Companion.timer
import no.nav.dagpenger.vedtak.modell.vedtak.Rammevedtak
import no.nav.dagpenger.vedtak.modell.vedtak.Utbetalingsvedtak
import no.nav.dagpenger.vedtak.modell.vedtak.Vedtak
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
    fun `200 OK og liste med alle vedtak for en person`() {
        personRepository.lagre(
            testPersonMed(
                rammevedtak(),
                utbetalingsvedtak(),
            ),
        )

        withVedtakApi {
            val response = client.post("/vedtak") {
                contentType(Json)
                setBody("""{"ident": "$ident"}""")
            }
            response.status shouldBe HttpStatusCode.OK
            response.contentType().toString() shouldContain "application/json"
            response.bodyAsText() shouldContain "Ramme"
            response.bodyAsText() shouldContain "Utbetaling"
        }
    }

    @Test
    internal fun `200 OK og tom liste hvis person ikke eksisterer i db`() {
        withVedtakApi {
            val response = client.post("/vedtak") {
                contentType(Json)
                setBody("""{"ident": "$ident"}""")
            }
            response.status shouldBe HttpStatusCode.OK
            response.contentType().toString() shouldContain "application/json"
            response.bodyAsText() shouldBe "[]"
        }
    }

    private fun withVedtakApi(
        personRepository: PersonRepository = this.personRepository,
        test: suspend ApplicationTestBuilder.() -> Unit,
    ) {
        testApplication {
            application { vedtakApi(personRepository) }
            test()
        }
    }

    private fun testPersonMed(vararg vedtak: Vedtak) = Person.rehydrer(
        ident = "12345123451".tilPersonIdentfikator(),
        vedtak = vedtak.toList(),
        aktivitetslogg = Aktivitetslogg(),
        perioder = emptyList(),
    )

    private fun utbetalingsvedtak() = Utbetalingsvedtak.utbetalingsvedtak(
        behandlingId = UUID.randomUUID(),
        utfall = true,
        vedtakstidspunkt = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS),
        virkningsdato = LocalDate.MAX,
        forbruk = Stønadsdager(10),
        utbetalingsdager = emptyList(),
        trukketEgenandel = 0.beløp,
    )

    private fun rammevedtak() = Rammevedtak.innvilgelse(
        behandlingId = UUID.randomUUID(),
        vedtakstidspunkt = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS),
        virkningsdato = LocalDate.MAX,
        dagsats = 1000.beløp,
        stønadsdager = Stønadsdager(104 * 5),
        dagpengerettighet = Dagpengerettighet.Ordinær,
        vanligArbeidstidPerDag = 8.timer,
        egenandel = 3500.beløp,
    )
}
