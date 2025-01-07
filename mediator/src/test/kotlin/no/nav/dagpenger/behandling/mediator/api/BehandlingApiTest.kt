package no.nav.dagpenger.behandling.mediator.api

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.navikt.tbd_libs.naisful.test.TestContext
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
import io.mockk.mockk
import io.mockk.slot
import io.mockk.spyk
import io.mockk.verify
import no.nav.dagpenger.avklaring.Avklaring
import no.nav.dagpenger.avklaring.Avklaringkode
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
import no.nav.dagpenger.behandling.modell.Behandling
import no.nav.dagpenger.behandling.modell.Ident.Companion.tilPersonIdentfikator
import no.nav.dagpenger.behandling.modell.Person
import no.nav.dagpenger.behandling.modell.hendelser.AvbrytBehandlingHendelse
import no.nav.dagpenger.behandling.modell.hendelser.AvklaringKvittertHendelse
import no.nav.dagpenger.behandling.modell.hendelser.BesluttBehandlingHendelse
import no.nav.dagpenger.behandling.modell.hendelser.GodkjennBehandlingHendelse
import no.nav.dagpenger.behandling.modell.hendelser.OpplysningSvarHendelse
import no.nav.dagpenger.behandling.modell.hendelser.SendTilbakeHendelse
import no.nav.dagpenger.opplysning.Faktum
import no.nav.dagpenger.opplysning.Opplysninger
import no.nav.dagpenger.opplysning.Saksbehandler
import no.nav.dagpenger.opplysning.Saksbehandlerkilde
import no.nav.dagpenger.opplysning.Systemkilde
import no.nav.dagpenger.opplysning.verdier.Barn
import no.nav.dagpenger.opplysning.verdier.BarnListe
import no.nav.dagpenger.opplysning.verdier.Beløp
import no.nav.dagpenger.regel.Minsteinntekt
import no.nav.dagpenger.regel.SøknadInnsendtHendelse
import no.nav.dagpenger.regel.Søknadstidspunkt
import no.nav.dagpenger.regel.Verneplikt.avtjentVerneplikt
import no.nav.dagpenger.uuid.UUIDv7
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime

internal class BehandlingApiTest {
    private val ident = "12345123451"
    private val rapid = spyk(TestRapid())
    private val hendelse =
        SøknadInnsendtHendelse(
            meldingsreferanseId = UUIDv7.ny(),
            ident = ident,
            søknadId = UUIDv7.ny(),
            gjelderDato = LocalDate.now(),
            fagsakId = 1,
            opprettet = LocalDateTime.now(),
        )

    private val avklaringer =
        listOf(
            Avklaring.rehydrer(
                UUIDv7.ny(),
                Avklaringkode("tittel 1", "beskrivelse ", "kanKvitteres"),
                mutableListOf(
                    Avklaring.Endring.Avbrutt(),
                ),
            ),
            Avklaring.rehydrer(
                UUIDv7.ny(),
                Avklaringkode("tittel 2", "beskrivelse ", "kanKvitteres"),
                mutableListOf(
                    Avklaring.Endring.Avklart(
                        avklartAv = Saksbehandlerkilde(UUIDv7.ny(), Saksbehandler("Z123456")),
                        begrunnelse = "heia",
                    ),
                ),
            ),
            Avklaring.rehydrer(
                UUIDv7.ny(),
                Avklaringkode("tittel 3", "beskrivelse ", "kanKvitteres"),
                mutableListOf(
                    Avklaring.Endring.Avklart(
                        avklartAv = Systemkilde(UUIDv7.ny(), LocalDateTime.now()),
                        begrunnelse = "heia",
                    ),
                ),
            ),
        )
    private val behandling =
        Behandling.rehydrer(
            behandlingId = UUIDv7.ny(),
            behandler = hendelse,
            gjeldendeOpplysninger =
                Opplysninger(
                    listOf(
                        Faktum(
                            avtjentVerneplikt,
                            true,
                        ),
                        Faktum(
                            opplysningstype = Søknadstidspunkt.søknadsdato,
                            verdi = LocalDate.now(),
                            kilde =
                                Saksbehandlerkilde(
                                    UUIDv7.ny(),
                                    Saksbehandler("Z123456"),
                                ),
                        ),
                        Faktum(
                            opplysningstype = Minsteinntekt.inntekt12,
                            verdi = Beløp(3000.034.toBigDecimal()),
                        ),
                        Faktum(
                            opplysningstype = TestOpplysningstyper.heltall,
                            verdi = 3,
                        ),
                        Faktum(
                            opplysningstype = TestOpplysningstyper.desimal,
                            verdi = 3.0,
                        ),
                        Faktum(
                            opplysningstype = TestOpplysningstyper.boolsk,
                            verdi = true,
                        ),
                        Faktum(
                            opplysningstype = TestOpplysningstyper.dato,
                            verdi = LocalDate.now(),
                        ),
                        Faktum(
                            opplysningstype = TestOpplysningstyper.beløpA,
                            verdi = Beløp(1000.toBigDecimal()),
                        ),
                        Faktum(
                            opplysningstype = TestOpplysningstyper.barn,
                            verdi =
                                BarnListe(
                                    listOf(
                                        Barn(
                                            LocalDate.now(),
                                            kvalifiserer = true,
                                        ),
                                    ),
                                ),
                            kilde = Saksbehandlerkilde(UUIDv7.ny(), Saksbehandler("Z123456")),
                        ),
                    ),
                ),
            basertPå = emptyList(),
            tilstand = Behandling.TilstandType.TilGodkjenning,
            sistEndretTilstand = LocalDateTime.now(),
            avklaringer = avklaringer,
        )

    private val person =
        Person(ident.tilPersonIdentfikator(), listOf(behandling))

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
            val behandlingDto = shouldNotThrowAny { objectMapper.readValue(response.bodyAsText(), BehandlingDTO::class.java) }
            behandlingDto.behandlingId shouldBe behandlingId
            behandlingDto.opplysning.shouldNotBeEmpty()
            behandlingDto.opplysning.all { it.redigerbar } shouldBe false
            behandlingDto.aktiveAvklaringer.shouldNotBeEmpty()

            val aktivAvklaring = behandling.aktiveAvklaringer().first()

            with(behandlingDto.aktiveAvklaringer.first()) {
                tittel shouldBe aktivAvklaring.kode.tittel
                beskrivelse shouldBe aktivAvklaring.kode.beskrivelse
                kode shouldBe aktivAvklaring.kode.kode
                id shouldBe aktivAvklaring.id
            }
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
    fun `test overgangene for behandling mellom saksbehandler og beslutter`() {
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
                hendelseMediator.behandle(any<GodkjennBehandlingHendelse>(), any())
            }

            // Send tilbake til saksbehandler
            autentisert(
                httpMethod = HttpMethod.Post,
                endepunkt = "/behandling/$behandlingId/send-tilbake",
                body = """{"ident":"09876543311"}""",
            ).status shouldBe HttpStatusCode.Created
            verify {
                hendelseMediator.behandle(any<SendTilbakeHendelse>(), any())
            }

            // Godkjenn igjen
            autentisert(
                httpMethod = HttpMethod.Post,
                endepunkt = "/behandling/$behandlingId/godkjenn",
                body = """{"ident":"09876543311"}""",
            ).status shouldBe HttpStatusCode.Created
            verify {
                hendelseMediator.behandle(any<GodkjennBehandlingHendelse>(), any())
            }

            // Beslutt
            autentisert(
                httpMethod = HttpMethod.Post,
                endepunkt = "/behandling/$behandlingId/beslutt",
                body = """{"ident":"09876543311"}""",
            ).status shouldBe HttpStatusCode.Created
            verify {
                hendelseMediator.behandle(any<BesluttBehandlingHendelse>(), any())
            }
        }
    }

    @Test
    @Disabled("Må finne ut av hvordan vente på rapid")
    fun `kan endre alle typer opplysninger som er redigerbare`() {
        medSikretBehandlingApi {
            val behandlingId = person.behandlinger().first().behandlingId
            val opplysninger =
                listOf(
                    Pair(TestOpplysningstyper.beløpA, 100),
                    Pair(TestOpplysningstyper.dato, LocalDate.of(2020, 1, 1)),
                    Pair(TestOpplysningstyper.heltall, 100),
                    Pair(TestOpplysningstyper.desimal, 100.12),
                    Pair(TestOpplysningstyper.boolsk, false),
                ).map { (opplysning, verdi) ->
                    Pair(
                        verdi,
                        person
                            .behandlinger()
                            .first()
                            .opplysninger()
                            .finnOpplysning(opplysning),
                    )
                }
            opplysninger.forEach { opplysning ->
                autentisert(
                    httpMethod = HttpMethod.Put,
                    endepunkt = "/behandling/$behandlingId/opplysning/${opplysning.second.id}",
                    // language=JSON
                    body = """{"begrunnelse":"tekst", "verdi": "${opplysning.first}" }""",
                ).status shouldBe HttpStatusCode.OK
            }
        }
    }

    @Test
    fun `kan ikke endre opplysninger som ikke er redigerbare`() {
        medSikretBehandlingApi {
            val behandlingId = person.behandlinger().first().behandlingId
            val opplysninger =
                listOf(
                    Pair(TestOpplysningstyper.barn, 100),
                ).map { (opplysning, verdi) ->
                    Pair(
                        verdi,
                        person
                            .behandlinger()
                            .first()
                            .opplysninger()
                            .finnOpplysning(opplysning),
                    )
                }
            opplysninger.forEach { opplysning ->
                autentisert(
                    httpMethod = HttpMethod.Put,
                    endepunkt = "/behandling/$behandlingId/opplysning/${opplysning.second.id}",
                    // language=JSON
                    body = """{"begrunnelse":"tekst", "verdi": "${opplysning.first}" }""",
                ).status shouldBe HttpStatusCode.BadRequest
            }
        }
    }

    // TODO: Legg til paramerisert test for alle opplysningstyper
    @Test
    @Disabled("Må finne ut av hvordan vente på rapid")
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
                    .finnOpplysning(TestOpplysningstyper.dato)
            val response =
                autentisert(
                    httpMethod = HttpMethod.Put,
                    endepunkt = "/behandling/$behandlingId/opplysning/${opplysning.id}",
                    // language=JSON
                    body = """{"begrunnelse":"tekst", "verdi": "2020-01-01" }""",
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
                            it.interestedIn("@utledetAv")
                        },
                        setOf(TestOpplysningstyper.dato),
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
            redigertOpplysning.verdi shouldBe LocalDate.parse("2020-01-01")
            // TODO: Legge til kilde  (redigertOpplysning.kilde as Saksbehandlerkilde).ident shouldBe "Z123456"
            redigertOpplysning.opplysningstype shouldBe TestOpplysningstyper.dato
        }
    }

    @Test
    fun `saksbehandler kan kvittere ut avklaring`() {
        medSikretBehandlingApi {
            val kvitteringHendelse = slot<AvklaringKvittertHendelse>()

            val behandlingId = person.behandlinger().first().behandlingId
            val avklaring =
                person
                    .behandlinger()
                    .first()
                    .aktiveAvklaringer()
                    .first()
            val response =
                autentisert(
                    httpMethod = HttpMethod.Put,
                    endepunkt = "/behandling/$behandlingId/avklaring/${avklaring.id}",
                    // language=JSON
                    body = """{"begrunnelse":"tekst"}""",
                )

            response.status shouldBe HttpStatusCode.NoContent

            verify {
                hendelseMediator.behandle(capture(kvitteringHendelse), any())
            }

            kvitteringHendelse.isCaptured shouldBe true
        }
    }

    private fun medSikretBehandlingApi(
        personRepository: PersonRepository = this.personRepository,
        hendelseMediator: HendelseMediator = this.hendelseMediator,
        test: suspend TestContext.() -> Unit,
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
