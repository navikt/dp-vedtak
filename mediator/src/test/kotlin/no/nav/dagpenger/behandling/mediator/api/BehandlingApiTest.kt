package no.nav.dagpenger.behandling.mediator.api

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers.test_support.TestRapid
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageProblems
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldBeEmpty
import io.kotest.matchers.string.shouldNotBeEmpty
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.ApplicationTestBuilder
import io.mockk.mockk
import io.mockk.slot
import io.mockk.spyk
import io.mockk.verify
import no.nav.dagpenger.behandling.TestOpplysningstyper
import no.nav.dagpenger.behandling.api.models.BehandlingDTO
import no.nav.dagpenger.behandling.api.models.KvitteringDTO
import no.nav.dagpenger.behandling.db.InMemoryPersonRepository
import no.nav.dagpenger.behandling.mediator.HendelseMediator
import no.nav.dagpenger.behandling.mediator.IMessageMediator
import no.nav.dagpenger.behandling.mediator.api.TestApplication.autentisert
import no.nav.dagpenger.behandling.mediator.api.TestApplication.testAzureAdToken
import no.nav.dagpenger.behandling.mediator.audit.Auditlogg
import no.nav.dagpenger.behandling.mediator.mottak.OpplysningSvarMessage
import no.nav.dagpenger.behandling.mediator.repository.PersonRepository
import no.nav.dagpenger.behandling.modell.Ident.Companion.tilPersonIdentfikator
import no.nav.dagpenger.behandling.modell.Person
import no.nav.dagpenger.behandling.modell.hendelser.AvbrytBehandlingHendelse
import no.nav.dagpenger.behandling.modell.hendelser.ForslagGodkjentHendelse
import no.nav.dagpenger.behandling.modell.hendelser.OpplysningSvar
import no.nav.dagpenger.behandling.modell.hendelser.OpplysningSvarHendelse
import no.nav.dagpenger.opplysning.Saksbehandlerkilde
import no.nav.dagpenger.opplysning.verdier.Beløp
import no.nav.dagpenger.regel.Minsteinntekt
import no.nav.dagpenger.regel.SøknadInnsendtHendelse
import no.nav.dagpenger.regel.Søknadstidspunkt
import no.nav.dagpenger.uuid.UUIDv7
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime

internal class BehandlingApiTest {
    private val ident = "12345123451"
    private val rapid = spyk(TestRapid())
    private val person =
        Person(ident.tilPersonIdentfikator()).also {
            it.håndter(
                SøknadInnsendtHendelse(
                    meldingsreferanseId = UUIDv7.ny(),
                    ident = ident,
                    søknadId = UUIDv7.ny(),
                    gjelderDato = LocalDate.now(),
                    fagsakId = 1,
                    opprettet = LocalDateTime.now(),
                ),
            )
            val behandlingId = it.behandlinger().first().behandlingId
            it.håndter(
                OpplysningSvarHendelse(
                    meldingsreferanseId = UUIDv7.ny(),
                    ident = ident,
                    behandlingId = behandlingId,
                    opprettet = LocalDateTime.now(),
                    opplysninger =
                        listOf(
                            OpplysningSvar(
                                opplysningstype = Søknadstidspunkt.søknadsdato,
                                verdi = LocalDate.now(),
                                tilstand = OpplysningSvar.Tilstand.Faktum,
                                kilde = Saksbehandlerkilde(UUIDv7.ny(), "Z123456"),
                            ),
                            OpplysningSvar(
                                opplysningstype = Minsteinntekt.inntekt12,
                                verdi = Beløp(3000.034.toBigDecimal()),
                                tilstand = OpplysningSvar.Tilstand.Faktum,
                                kilde = Saksbehandlerkilde(UUIDv7.ny(), "Z123456"),
                            ),
                            OpplysningSvar(
                                opplysningstype = TestOpplysningstyper.tekst,
                                verdi = "DETTE ERN TEST ",
                                tilstand = OpplysningSvar.Tilstand.Faktum,
                                kilde = Saksbehandlerkilde(UUIDv7.ny(), "Z123456"),
                            ),
                            OpplysningSvar(
                                opplysningstype = TestOpplysningstyper.heltall,
                                verdi = 3,
                                tilstand = OpplysningSvar.Tilstand.Faktum,
                                kilde = Saksbehandlerkilde(UUIDv7.ny(), "Z123456"),
                            ),
                        ),
                ),
            )
        }
    private val personRepository =
        InMemoryPersonRepository().also {
            it.lagre(person)
        }
    private val hendelseMediator = mockk<HendelseMediator>(relaxed = true)
    private val auditlogg = mockk<Auditlogg>(relaxed = true)

    @AfterEach
    fun tearDown() {
        personRepository.reset()
    }

    @Test
    fun `ikke autentiserte kall returnerer 401`() {
        medSikretBehandlingApi {
            val response =
                client.post("/behandling") {
                    setBody("""{"ident":"$ident"}""")
                }
            response.status shouldBe HttpStatusCode.Unauthorized
        }
    }

    @Test
    fun `kall uten saksbehandlingsADgruppe i claims returnerer 401`() {
        medSikretBehandlingApi {
            autentisert(
                token = testAzureAdToken(ADGrupper = emptyList()),
                endepunkt = "/behandling",
                body = """{"ident":"$ident"}""",
            ).status shouldBe HttpStatusCode.Unauthorized

            autentisert(
                token = testAzureAdToken(ADGrupper = listOf("ikke-saksbehandler")),
                endepunkt = "/behandling",
                body = """{"ident":"$ident"}""",
            ).status shouldBe HttpStatusCode.Unauthorized

            autentisert(
                token = testAzureAdToken(ADGrupper = listOf("dagpenger-saksbehandler")),
                endepunkt = "/behandling",
                body = """{"ident":"$ident"}""",
            ).status shouldBe HttpStatusCode.OK
        }
    }

    @Test
    fun `hent behandlinger gitt person`() {
        medSikretBehandlingApi {
            val response = autentisert("/behandling", body = """{"ident":"$ident"}""")
            response.status shouldBe HttpStatusCode.OK
            response.bodyAsText().shouldNotBeEmpty()
            verify {
                auditlogg.les(any(), any(), any())
            }
        }
    }

    @Test
    fun `gir 404 hvis person ikke eksisterer`() {
        medSikretBehandlingApi {
            val response = autentisert("/behandling", body = """{"ident":"09876543311"}""")
            response.status shouldBe HttpStatusCode.NotFound
        }
    }

    @Test
    fun `hent behandling gitt behandlingId`() {
        medSikretBehandlingApi {
            val behandlingId = person.behandlinger().first().behandlingId
            val response = autentisert(httpMethod = HttpMethod.Get, endepunkt = "/behandling/$behandlingId")
            response.status shouldBe HttpStatusCode.OK
            response.bodyAsText().shouldNotBeEmpty()
            val behandling = shouldNotThrowAny { objectMapper.readValue(response.bodyAsText(), BehandlingDTO::class.java) }
            behandling.behandlingId shouldBe behandlingId
            behandling.opplysning.shouldNotBeEmpty()
            behandling.opplysning.all { it.redigerbar } shouldBe false
            verify {
                auditlogg.les(any(), any(), any())
            }
        }
    }

    @Test
    fun `avbryt behandling gitt behandlingId`() {
        medSikretBehandlingApi {
            val behandlingId = person.behandlinger().first().behandlingId
            val response =
                autentisert(
                    httpMethod = HttpMethod.Post,
                    endepunkt = "/behandling/$behandlingId/avbryt",
                    body = """{"ident":"09876543311"}""",
                )
            response.status shouldBe HttpStatusCode.Created
            response.bodyAsText().shouldBeEmpty()
            verify {
                hendelseMediator.behandle(any<AvbrytBehandlingHendelse>(), any())
            }
        }
    }

    @Test
    fun `godkjenn behandling gitt behandlingId`() {
        medSikretBehandlingApi {
            val behandlingId = person.behandlinger().first().behandlingId
            val response =
                autentisert(
                    httpMethod = HttpMethod.Post,
                    endepunkt = "/behandling/$behandlingId/godkjenn",
                    body = """{"ident":"09876543311"}""",
                )
            response.status shouldBe HttpStatusCode.Created
            response.bodyAsText().shouldBeEmpty()
            verify {
                hendelseMediator.behandle(any<ForslagGodkjentHendelse>(), any())
            }
        }
    }

    @Test
    fun `endre opplysningsverdi`() {
        medSikretBehandlingApi {
            val messageMediator = mockk<IMessageMediator>(relaxed = true)
            val opplysningSvar = slot<String>()
            val opplysningSvarHendelse = slot<OpplysningSvarHendelse>()

            val behandlingId = person.behandlinger().first().behandlingId
            val opplysning =
                person
                    .behandlinger()
                    .first()
                    .opplysninger()
                    .finnOpplysning(TestOpplysningstyper.heltall)
            val response =
                autentisert(
                    httpMethod = HttpMethod.Put,
                    endepunkt = "/behandling/$behandlingId/opplysning/${opplysning.id}",
                    // language=JSON
                    body = """{"begrunnelse":"tekst", "verdi": 4 }""",
                )
            response.status shouldBe HttpStatusCode.OK
            with(response.bodyAsText()) {
                shouldNotBeEmpty()
                shouldNotThrowAny {
                    val kvitteringDTO = objectMapper.readValue<KvitteringDTO>(this)
                    kvitteringDTO.behandlingId shouldBe behandlingId
                }
            }

            verify {
                rapid.publish(capture(opplysningSvar))
                auditlogg.oppdater(any(), any(), any())
            }
            opplysningSvar.isCaptured shouldBe true
            val melding =
                opplysningSvar.captured.let { json ->
                    OpplysningSvarMessage(
                        JsonMessage(json, MessageProblems(json), mockk(relaxed = true)).also {
                            it.requireKey("ident", "behandlingId", "@løsning")
                        },
                        setOf(TestOpplysningstyper.heltall),
                    )
                }

            melding.behandle(
                messageMediator,
                mockk(),
            )

            verify {
                messageMediator.behandle(capture(opplysningSvarHendelse), any(), any())
            }

            opplysningSvarHendelse.isCaptured shouldBe true
            val redigertOpplysning = opplysningSvarHendelse.captured.opplysninger.first()
            redigertOpplysning.verdi shouldBe 4
            // TODO: Legge til kilde  (redigertOpplysning.kilde as Saksbehandlerkilde).ident shouldBe "Z123456"
            redigertOpplysning.opplysningstype shouldBe TestOpplysningstyper.heltall
        }
    }

    private fun medSikretBehandlingApi(
        personRepository: PersonRepository = this.personRepository,
        hendelseMediator: HendelseMediator = this.hendelseMediator,
        test: suspend ApplicationTestBuilder.() -> Unit,
    ) {
        System.setProperty("Grupper.saksbehandler", "dagpenger-saksbehandler")
        TestApplication.withMockAuthServerAndTestApplication(
            moduleFunction = {
                behandlingApi(personRepository, hendelseMediator, auditlogg, emptySet()) { rapid }
            },
            test,
        )
        System.clearProperty("Grupper.saksbehandler")
    }

    private companion object {
        private val objectMapper =
            jacksonObjectMapper()
                .registerModule(JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    }
}
