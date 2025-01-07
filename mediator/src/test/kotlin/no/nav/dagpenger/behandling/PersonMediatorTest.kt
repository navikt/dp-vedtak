package no.nav.dagpenger.behandling

import com.fasterxml.jackson.databind.JsonNode
import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers.asLocalDate
import com.github.navikt.tbd_libs.rapids_and_rivers.asLocalDateTime
import com.github.navikt.tbd_libs.rapids_and_rivers.test_support.TestRapid
import com.github.navikt.tbd_libs.rapids_and_rivers.toUUID
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.assertions.withClue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import no.nav.dagpenger.avklaring.Avklaring
import no.nav.dagpenger.avklaring.Avklaringkode
import no.nav.dagpenger.behandling.db.Postgres.withMigratedDb
import no.nav.dagpenger.behandling.konfigurasjon.Feature
import no.nav.dagpenger.behandling.konfigurasjon.skruAvFeatures
import no.nav.dagpenger.behandling.konfigurasjon.skruPåFeature
import no.nav.dagpenger.behandling.mediator.BehovMediator
import no.nav.dagpenger.behandling.mediator.HendelseMediator
import no.nav.dagpenger.behandling.mediator.MessageMediator
import no.nav.dagpenger.behandling.mediator.lagVedtak
import no.nav.dagpenger.behandling.mediator.melding.PostgresHendelseRepository
import no.nav.dagpenger.behandling.mediator.repository.AvklaringKafkaObservatør
import no.nav.dagpenger.behandling.mediator.repository.AvklaringRepositoryPostgres
import no.nav.dagpenger.behandling.mediator.repository.BehandlingRepositoryPostgres
import no.nav.dagpenger.behandling.mediator.repository.OpplysningerRepositoryPostgres
import no.nav.dagpenger.behandling.mediator.repository.PersonRepository
import no.nav.dagpenger.behandling.mediator.repository.PersonRepositoryPostgres
import no.nav.dagpenger.behandling.mediator.toMap
import no.nav.dagpenger.behandling.modell.Behandling
import no.nav.dagpenger.behandling.modell.Behandling.TilstandType.UnderOpprettelse
import no.nav.dagpenger.behandling.modell.BehandlingObservatør.BehandlingEndretTilstand
import no.nav.dagpenger.behandling.modell.Ident.Companion.tilPersonIdentfikator
import no.nav.dagpenger.behandling.modell.Person
import no.nav.dagpenger.behandling.modell.PersonObservatør
import no.nav.dagpenger.behandling.modell.hendelser.AvklaringKvittertHendelse
import no.nav.dagpenger.behandling.modell.hendelser.BesluttBehandlingHendelse
import no.nav.dagpenger.behandling.modell.hendelser.GodkjennBehandlingHendelse
import no.nav.dagpenger.behandling.modell.hendelser.SendTilbakeHendelse
import no.nav.dagpenger.opplysning.Saksbehandler
import no.nav.dagpenger.regel.Behov.AndreØkonomiskeYtelser
import no.nav.dagpenger.regel.Behov.Barnetillegg
import no.nav.dagpenger.regel.Behov.Foreldrepenger
import no.nav.dagpenger.regel.Behov.HelseTilAlleTyperJobb
import no.nav.dagpenger.regel.Behov.Inntekt
import no.nav.dagpenger.regel.Behov.KanJobbeDeltid
import no.nav.dagpenger.regel.Behov.KanJobbeHvorSomHelst
import no.nav.dagpenger.regel.Behov.Lønnsgaranti
import no.nav.dagpenger.regel.Behov.Omsorgspenger
import no.nav.dagpenger.regel.Behov.OppgittAndreYtelserUtenforNav
import no.nav.dagpenger.regel.Behov.Opplæringspenger
import no.nav.dagpenger.regel.Behov.OpptjeningsperiodeFraOgMed
import no.nav.dagpenger.regel.Behov.Ordinær
import no.nav.dagpenger.regel.Behov.Permittert
import no.nav.dagpenger.regel.Behov.PermittertFiskeforedling
import no.nav.dagpenger.regel.Behov.Pleiepenger
import no.nav.dagpenger.regel.Behov.RegistrertSomArbeidssøker
import no.nav.dagpenger.regel.Behov.SisteAvsluttendeKalenderMåned
import no.nav.dagpenger.regel.Behov.Svangerskapspenger
import no.nav.dagpenger.regel.Behov.Sykepenger
import no.nav.dagpenger.regel.Behov.TarUtdanningEllerOpplæring
import no.nav.dagpenger.regel.Behov.Verneplikt
import no.nav.dagpenger.regel.Behov.VilligTilÅBytteYrke
import no.nav.dagpenger.regel.Behov.ØnskerDagpengerFraDato
import no.nav.dagpenger.regel.Behov.ØnsketArbeidstid
import no.nav.dagpenger.regel.RegelverkDagpenger
import no.nav.dagpenger.uuid.UUIDv7
import org.approvaltests.Approvals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period

internal class PersonMediatorTest {
    private val rapid = TestRapid()
    private val ident = "11109233444"
    private val personRepository =
        PersonRepositoryPostgres(
            BehandlingRepositoryPostgres(
                OpplysningerRepositoryPostgres(),
                AvklaringRepositoryPostgres(AvklaringKafkaObservatør(rapid)),
            ),
        )

    private val testObservatør = TestObservatør()

    private val hendelseMediator =
        HendelseMediator(
            personRepository = personRepository,
            behovMediator = BehovMediator(),
            aktivitetsloggMediator = mockk(relaxed = true),
            observatører = listOf(testObservatør),
        )

    init {
        MessageMediator(
            rapidsConnection = rapid,
            hendelseMediator = hendelseMediator,
            hendelseRepository = PostgresHendelseRepository(),
            opplysningstyper = RegelverkDagpenger.produserer,
        )
    }

    @BeforeEach
    fun setUp() {
        rapid.reset()
        skruAvFeatures()
    }

    @Test
    fun `kan bare lage en behandling for samme søknad`() {
        withMigratedDb {
            val testPerson =
                TestPerson(
                    ident,
                    rapid,
                    søknadsdato = 6.mai(2021),
                )
            testPerson.sendSøknad()
            rapid.harHendelse("behandling_opprettet", offset = 2)
            testPerson.sendSøknad()
            personRepository.hent(ident.tilPersonIdentfikator()).also {
                it.shouldNotBeNull()
                it.behandlinger().size shouldBe 1
            }
        }
    }

    @Test
    fun `søknad med for høy alder skal automatisk avslås`() =
        withMigratedDb {
            skruPåFeature(Feature.INNVILGELSE)
            val testPerson =
                TestPerson(
                    ident,
                    rapid,
                    søknadsdato = 6.mai(2021),
                    alder = 76,
                )
            løsbehandlingFramTilAlder(testPerson)

            personRepository.hent(ident.tilPersonIdentfikator()).also {
                it.shouldNotBeNull()
                it.behandlinger().size shouldBe 1
            }

            rapid.harHendelse("vedtak_fattet") {
                medMeldingsInnhold("fastsatt") {
                    medBoolsk("utfall") shouldBe false
                }
                medNode("vilkår").size() shouldBe 3
            }

            godkjennOpplysninger("avslag")

            vedtakJson()
        }

    @Test
    fun `søknad med for lite inntekt skal automatisk avslås`() =
        withMigratedDb {
            skruPåFeature(Feature.INNVILGELSE)
            val testPerson =
                TestPerson(
                    ident,
                    rapid,
                    søknadsdato = 6.mai(2021),
                )
            løsBehandlingFramTilMinsteinntekt(testPerson)

            personRepository.hent(ident.tilPersonIdentfikator()).also {
                it.shouldNotBeNull()
                it.behandlinger().size shouldBe 1
                it.aktivBehandling.aktivAvklaringer.shouldHaveSize(6)
            }

            godkjennOpplysninger("avslag")
            testPerson.markerAvklaringerIkkeRelevant(åpneAvklaringer())

            val person =
                personRepository.hent(ident.tilPersonIdentfikator()).also {
                    it.shouldNotBeNull()
                    it.aktivBehandling.aktivAvklaringer.shouldBeEmpty()
                }

            rapid.harHendelse("vedtak_fattet", 2) {
                medMeldingsInnhold("fastsatt") {
                    medBoolsk("utfall") shouldBe false
                }
                medNode("fagsakId").asInt() shouldBe 123
                medTekst("søknadId") shouldBe testPerson.søknadId
                medTekst("ident") shouldBe ident
                medBoolsk("automatisk") shouldBe true
                shouldNotBeNull {
                    medDato("virkningsdato")
                }
                shouldNotBeNull {
                    medTimestamp("vedtakstidspunkt")
                }
                medTekst("behandlingId") shouldBe person!!.aktivBehandling.behandlingId.toString()
                medVilkår("Oppfyller kravet til alder") {
                    erOppfylt()
                }
                medVilkår("Oppfyller kravet til minsteinntekt eller verneplikt") {
                    erIkkeOppfylt()
                }
                medVilkår("Krav til arbeidssøker") {
                    erOppfylt()
                }
                medVilkår("Registrert som arbeidssøker på søknadstidspunktet") {
                    erOppfylt()
                }
                medVilkår("Rettighetstype") {
                    erOppfylt()
                }
            }

            rapid.inspektør.size shouldBe 23

            testObservatør.tilstandsendringer.size shouldBe 3

            repeat(rapid.inspektør.size) {
                withClue("Melding nr $it skal ha nøkkel. Meldingsinnhold: ${rapid.inspektør.message(it)}") {
                    rapid.inspektør.key(it) shouldBe ident
                }
            }
        }

    @Test
    fun `Søknad med nok inntekt skal avbrytes`() =
        withMigratedDb {
            val testPerson =
                TestPerson(
                    ident,
                    rapid,
                    søknadsdato = 6.mai(2021),
                    InntektSiste12Mnd = 500000,
                )
            løsBehandlingFramTilAvbruddInntekt(testPerson)

            godkjennOpplysninger("knokcout")

            rapid.harHendelse("behandling_avbrutt") {
                medTekst("årsak") shouldBe "Førte ikke til avslag på grunn av inntekt"
            }

            rapid.inspektør.size shouldBe 14
        }

    @Test
    fun `Søknad med nok inntekt skal innvilges`() =
        withMigratedDb {
            val testPerson =
                TestPerson(
                    ident,
                    rapid,
                    søknadsdato = 6.mai(2021),
                    InntektSiste12Mnd = 500000,
                )
            val saksbehandler = TestSaksbehandler(testPerson, hendelseMediator, personRepository, rapid)
            skruPåFeature(Feature.INNVILGELSE)
            løsBehandlingFramTilInnvilgelse(testPerson)

            godkjennOpplysninger("etterInntekt")

            rapid.harHendelse("forslag_til_vedtak") {
                medFastsettelser {
                    oppfylt
                }
            }

            // TODO: Beregningsmetode for tapt arbeidstid har defaultverdi for testing av innvilgelse og derfor mangler avklaringen
            rapid.inspektør.size shouldBe 22

            rapid.harHendelse("forslag_til_vedtak") {
                medFastsettelser {
                    oppfylt
                }
            }

            personRepository.hent(ident.tilPersonIdentfikator()).also {
                it.shouldNotBeNull()
                it.behandlinger().first().kreverTotrinnskontroll() shouldBe true
            }

            // Saksbehandler lukker alle avklaringer
            saksbehandler.lukkAlleAvklaringer()

            saksbehandler.godkjenn()
            saksbehandler.beslutt()

            rapid.harHendelse("vedtak_fattet") {
                medBoolsk("automatisk") shouldBe false
                medFastsettelser {
                    oppfylt
                    withClue("Grunnlag bør større enn 0") { grunnlag shouldBeGreaterThan 0 }
                    vanligArbeidstidPerUke shouldBe 37.5
                    sats shouldBeGreaterThan 0
                    samordning.shouldNotBeEmpty()
                    samordning.first()["type"].asText() shouldBe "Sykepenger dagsats"
                }
                medNode("behandletAv")
                    .map {
                        it["behandler"]["ident"].asText()
                    }.shouldContainExactlyInAnyOrder("NAV987987", "NAV123123")
            }

            vedtakJson()
        }

    @Test
    fun `Søknad med som oppfyller kravet til verneplikt skal innvilges`() =
        withMigratedDb {
            val testPerson =
                TestPerson(
                    ident,
                    rapid,
                    søknadsdato = 6.mai(2021),
                    InntektSiste12Mnd = 50000,
                    søkerVerneplikt = true,
                )
            val saksbehandler = TestSaksbehandler(testPerson, hendelseMediator, personRepository, rapid)
            skruPåFeature(Feature.INNVILGELSE)
            løsBehandlingFramTilInnvilgelse(testPerson)

            rapid.harHendelse("forslag_til_vedtak") {
                medFastsettelser {
                    oppfylt
                }
            }

            personRepository.hent(ident.tilPersonIdentfikator()).also {
                it.shouldNotBeNull()
                it.behandlinger().first().kreverTotrinnskontroll() shouldBe true
            }

            // Saksbehandler lukker alle avklaringer
            saksbehandler.lukkAlleAvklaringer()

            saksbehandler.godkjenn()
            saksbehandler.beslutt()

            rapid.harHendelse("vedtak_fattet") {
                medBoolsk("automatisk") shouldBe false
                medFastsettelser {
                    oppfylt
                    vanligArbeidstidPerUke shouldBe 37.5
                    grunnlag shouldBe 319197
                    periode("Dagpengeperiode").shouldBeNull()
                    periode("Verneplikt") shouldBe 26
                    sats shouldBeGreaterThan 0
                }
                medNode("behandletAv")
                    .map {
                        it["behandler"]["ident"].asText()
                    }.shouldContainExactlyInAnyOrder("NAV987987", "NAV123123")
            }

            vedtakJson()
        }

    @Test
    fun `Behandling sendt til kontroll`() =
        withMigratedDb {
            val testPerson =
                TestPerson(
                    ident,
                    rapid,
                    søknadsdato = 6.mai(2021),
                    InntektSiste12Mnd = 500000,
                )
            val saksbehandler =
                TestSaksbehandler(
                    testPerson,
                    hendelseMediator,
                    personRepository,
                    rapid,
                )
            skruPåFeature(Feature.INNVILGELSE)
            løsBehandlingFramTilInnvilgelse(testPerson)

            rapid.harHendelse("forslag_til_vedtak") {
                medFastsettelser {
                    this.oppfylt
                }
            }

            saksbehandler.lukkAlleAvklaringer()

            saksbehandler.godkjenn()

            rapid.harHendelse("behandling_endret_tilstand") {
                medTekst("gjeldendeTilstand") shouldBe "TilBeslutning"
            }

            shouldThrow<IllegalStateException> { testPerson.avbrytBehandling() }

            saksbehandler.sendTilbake()

            saksbehandler.godkjenn()
            saksbehandler.beslutt()

            rapid.harHendelse("vedtak_fattet")
        }

    @Test
    fun `søknad som slår ut på manuelle behandling må føre til forslag til vedtak`() =
        withMigratedDb {
            val testPerson =
                TestPerson(
                    ident,
                    rapid,
                    søknadsdato = 6.mai(2021),
                )

            løsBehandlingFramTilMinsteinntekt(testPerson)

            rapid.harHendelse("forslag_til_vedtak") {
                åpneAvklaringer().values shouldHaveSize 6
            }
            rapid.inspektør.size shouldBe
                listOf(
                    "opprettet" to 1,
                    "behov" to 5,
                    "avklaring" to 6,
                    "forslag" to 1,
                    "event" to 2,
                ).sumOf { it.second }
        }

    @Test
    fun `e2e av søknad som blir avbrutt `() =
        withMigratedDb {
            val testPerson =
                TestPerson(
                    ident,
                    rapid,
                    søknadsdato = 6.mai(2021),
                )
            testPerson.sendSøknad()
            rapid.harHendelse("behandling_opprettet", offset = 2)

            /**
             * Avbryter behandlingen før svar på manuell behandling (feks skjerming av person)
             */
            testPerson.avbrytBehandling()

            rapid.harHendelse("behandling_avbrutt") {
                medTekst("søknadId") shouldBe testPerson.søknadId
            }

            rapid.inspektør.size shouldBe 5
        }

    @Test
    fun `søker før ny rapporteringsfrist, men ønsker etter`() =
        withMigratedDb {
            val testPerson =
                TestPerson(
                    ident,
                    rapid,
                    søknadsdato = 5.juni(2024),
                    innsendt = 5.juni(2024).atTime(12, 0),
                    ønskerFraDato = 10.juni(2024),
                )
            løsBehandlingFramTilMinsteinntekt(testPerson)

            rapid.harHendelse("forslag_til_vedtak")

            åpneAvklaringer().values.shouldContainExactlyInAnyOrder(
                listOf(
                    "EØSArbeid",
                    "HattLukkedeSakerSiste8Uker",
                    "InntektNesteKalendermåned",
                    "JobbetUtenforNorge",
                    "MuligGjenopptak",
                    "SvangerskapsrelaterteSykepenger",
                    "ØnskerEtterRapporteringsfrist",
                ),
            )
        }

    @Test
    fun `søker etter overgang til ny rapporteringsfrist`() =
        withMigratedDb {
            val testPerson =
                TestPerson(
                    ident,
                    rapid,
                    søknadsdato = 7.juni(2024),
                    innsendt = 7.juni(2024).atTime(12, 0),
                    ønskerFraDato = 10.juni(2024),
                )
            løsBehandlingFramTilMinsteinntekt(testPerson)

            rapid.harHendelse("forslag_til_vedtak")

            åpneAvklaringer().values.shouldContainExactlyInAnyOrder(
                listOf(
                    "EØSArbeid",
                    "HattLukkedeSakerSiste8Uker",
                    "InntektNesteKalendermåned",
                    "JobbetUtenforNorge",
                    "MuligGjenopptak",
                    "SvangerskapsrelaterteSykepenger",
                ),
            )
        }

    @Test
    fun `søker for langt fram i tid`() =
        withMigratedDb {
            val testPerson =
                TestPerson(
                    ident,
                    rapid,
                    søknadsdato = 1.juni(2024),
                    innsendt = 1.juni(2024).atTime(12, 0),
                    ønskerFraDato = 30.juni(2024),
                )
            løsBehandlingFramTilMinsteinntekt(testPerson)

            rapid.harHendelse("forslag_til_vedtak")
            åpneAvklaringer().values.shouldContainExactlyInAnyOrder(
                listOf(
                    "EØSArbeid",
                    "HattLukkedeSakerSiste8Uker",
                    "InntektNesteKalendermåned",
                    "JobbetUtenforNorge",
                    "MuligGjenopptak",
                    "SvangerskapsrelaterteSykepenger",
                    "SøknadstidspunktForLangtFramITid",
                    "ØnskerEtterRapporteringsfrist",
                ),
            )
        }

    @Test
    fun `endring av prøvingsdato`() {
        withMigratedDb {
            skruPåFeature(Feature.INNVILGELSE)
            val testPerson =
                TestPerson(
                    ident,
                    rapid,
                    søknadsdato = 1.juni(2024),
                    innsendt = 1.juni(2024).atTime(12, 0),
                    InntektSiste12Mnd = 500000,
                    ønskerFraDato = 1.juni(2024),
                )
            løsBehandlingFramTilInnvilgelse(testPerson)

            rapid.harHendelse("forslag_til_vedtak") {
                medFastsettelser {
                    oppfylt
                }
                medOpplysning<LocalDate>("Prøvingsdato") shouldBe 1.juni(2024)
            }

            godkjennOpplysninger("innvilgelse")

            // Setter ny prøvingsdato (som kalles Virkningsdato for bakoverkompabilitet med behovsløsere)
            val nyPrøvingsdato = 22.juli(2024)
            testPerson.prøvingsdato = nyPrøvingsdato
            testPerson.endreOpplysning("Virkningsdato", nyPrøvingsdato)

            rapid.harBehov("RegistrertSomArbeidssøker") {
                medDato("Virkningsdato") shouldBe nyPrøvingsdato
            }

            rapid.harBehov(Inntekt) {
                medDato("Virkningsdato") shouldBe nyPrøvingsdato
            }
            testPerson.løsBehov(
                RegistrertSomArbeidssøker,
                Sykepenger,
                Omsorgspenger,
                Svangerskapspenger,
                Foreldrepenger,
                Opplæringspenger,
                Pleiepenger,
                OppgittAndreYtelserUtenforNav,
                AndreØkonomiskeYtelser,
                Inntekt,
            )

            rapid.harHendelse("forslag_til_vedtak") {
                medFastsettelser {
                    oppfylt
                }
                medOpplysning<LocalDate>("Prøvingsdato") shouldBe nyPrøvingsdato
            }

            withClue("Skal kun ha opplysninger nødvendig for innvilgelse") {
                godkjennOpplysninger("innvilgelse-igjen")
            }

            // Setter ny prøvingsdato (som kalles Virkningsdato for bakoverkompabilitet med behovsløsere)
            val endaNyerePrøvingsdato = 22.august(2024)
            testPerson.prøvingsdato = endaNyerePrøvingsdato
            testPerson.InntektSiste12Mnd = 0
            testPerson.endreOpplysning("Virkningsdato", endaNyerePrøvingsdato)

            rapid.harBehov("RegistrertSomArbeidssøker") {
                medDato("Virkningsdato") shouldBe endaNyerePrøvingsdato
            }

            rapid.harBehov(Inntekt) {
                medDato("Virkningsdato") shouldBe endaNyerePrøvingsdato
            }

            testPerson.løsBehov(
                RegistrertSomArbeidssøker,
                Sykepenger,
                Omsorgspenger,
                Svangerskapspenger,
                Foreldrepenger,
                Opplæringspenger,
                Pleiepenger,
                OppgittAndreYtelserUtenforNav,
                AndreØkonomiskeYtelser,
                Inntekt,
            )

            rapid.harHendelse("forslag_til_vedtak") {
                medFastsettelser {
                    `ikke oppfylt`
                }
                medOpplysning<LocalDate>("Prøvingsdato") shouldBe endaNyerePrøvingsdato
            }

            withClue("Skal kun ha opplysninger nødvendig for avslag") {
                godkjennOpplysninger("avslag")
            }
        }
    }

    @Test
    fun `publiserer tilstandsendinger`() =
        withMigratedDb {
            val person = TestPerson(ident, rapid)

            person.sendSøknad()

            rapid.inspektør.message(0).run {
                this["@event_name"].asText() shouldBe "behandling_endret_tilstand"
                this["ident"].asText() shouldBe ident
                this["forrigeTilstand"].asText() shouldBe UnderOpprettelse.name
                Duration.parse(this["tidBrukt"].asText()).shouldBeGreaterThan(Duration.ZERO)
            }
        }

    private fun vedtakJson() =
        personRepository.hent(ident.tilPersonIdentfikator()).run {
            shouldNotBeNull()

            behandlinger().first().run {
                val vedtak =
                    lagVedtak(
                        behandlingId,
                        ident = behandler.ident.tilPersonIdentfikator(),
                        søknadId = behandler.eksternId,
                        opplysninger = opplysninger(),
                        automatisk = erAutomatiskBehandlet(),
                        godkjentAv = godkjent,
                        besluttetAv = besluttet,
                    )

                // Dette er vedtaket som brukes i dp-arena-sink: vedtak_fattet_innvilgelse.json
                JsonMessage.newMessage("vedtak_fattet", vedtak.toMap()).toJson()
            }
        }

    enum class Behandlingslengde {
        Alder,
        AvbruddInntekt,
        Minsteinntekt,
        KravPåDagpenger,
    }

    private fun løsbehandlingFramTilAlder(testPerson: TestPerson) {
        løsBehandlingFramTil(testPerson, Behandlingslengde.Alder)
    }

    private fun løsBehandlingFramTilMinsteinntekt(testPerson: TestPerson) {
        løsBehandlingFramTil(testPerson, Behandlingslengde.Minsteinntekt)
    }

    private fun løsBehandlingFramTilAvbruddInntekt(testPerson: TestPerson) {
        løsBehandlingFramTil(testPerson, Behandlingslengde.AvbruddInntekt)
    }

    private fun løsBehandlingFramTilInnvilgelse(testPerson: TestPerson) {
        løsBehandlingFramTil(testPerson, Behandlingslengde.KravPåDagpenger)
    }

    private fun løsBehandlingFramTil(
        testPerson: TestPerson,
        behandlingslengde: Behandlingslengde,
    ) {
        testPerson.sendSøknad()
        rapid.harHendelse("behandling_opprettet", offset = 2)

        /**
         * Innhenter rettighetstype
         */
        rapid.harBehov(Ordinær, Permittert, Lønnsgaranti, PermittertFiskeforedling)

        /**
         * Fastsetter søknadsdato
         */
        rapid.harBehov("Søknadsdato") {
            medTekst("søknadId") shouldBe testPerson.søknadId
            medTekst("søknad_uuid") shouldBe testPerson.søknadId

            // @utledetAv flyttes ut av behov og opp i egen nøkkel på rot i pakka
            medNode("@utledetAv").shouldBeNull()
        }

        rapid.harBehov("Fødselsdato", "Søknadsdato", ØnskerDagpengerFraDato)
        testPerson.løsBehov(
            "Fødselsdato",
            "Søknadsdato",
            ØnskerDagpengerFraDato,
            Ordinær,
            Permittert,
            Lønnsgaranti,
            PermittertFiskeforedling,
        )

        /**
         * Sjekker kravet til registrering som arbeidssøker
         */
        rapid.harBehov(RegistrertSomArbeidssøker)
        testPerson.løsBehov(RegistrertSomArbeidssøker)

        if (behandlingslengde == Behandlingslengde.Alder) {
            return
        }

        /**
         * Fastsetter opptjeningsperiode og inntekt. Pt brukes opptjeningsperiode generert fra dp-inntekt
         */
        rapid.harBehov(Inntekt) {
            // TODO: Dette er nå prøvingsdato og bør bytte navn overalt
            medDato("Virkningsdato") shouldBe maxOf(testPerson.søknadsdato, testPerson.ønskerFraDato)
            /**
             * TODO: Vi må ta vekk opptjeningsperiode fra dp-inntekt og skive om måten dp-inntekt lagrer inntekt på beregningsdato
             * medDato(OpptjeningsperiodeFraOgMed) shouldBe 1.april(2018)
             * opptjeningsperiodeEr(måneder = 36)
             */
        }
        testPerson.løsBehov(Inntekt)

        /**
         * Sjekker om mulig verneplikt
         */
        rapid.harBehov(Verneplikt)
        testPerson.løsBehov(Verneplikt)

        if (behandlingslengde == Behandlingslengde.AvbruddInntekt) {
            return
        }

        /**
         * Sjekker kravene til reell arbeidssøker
         */
        rapid.harBehov(KanJobbeDeltid, KanJobbeHvorSomHelst, HelseTilAlleTyperJobb, VilligTilÅBytteYrke)
        testPerson.løsBehov(KanJobbeDeltid, KanJobbeHvorSomHelst, HelseTilAlleTyperJobb, VilligTilÅBytteYrke)

        if (behandlingslengde == Behandlingslengde.Minsteinntekt) {
            return
        }

        /**
         * Innhenter tar utdanning eller opplæring
         */
        rapid.harBehov(TarUtdanningEllerOpplæring)
        testPerson.løsBehov(TarUtdanningEllerOpplæring)

        /**
         * Henter inn barn som kan gi barnetillegg
         */
        rapid.harBehov(Barnetillegg)
        testPerson.løsBehov(Barnetillegg)

        /**
         * Henter inn barn som kan gi barnetillegg
         */
        rapid.harBehov(ØnsketArbeidstid)
        testPerson.løsBehov(ØnsketArbeidstid)

        /**
         * Innhente informasjon om andre ytelser
         */
        rapid.harBehov(
            Sykepenger,
            Omsorgspenger,
            Svangerskapspenger,
            Foreldrepenger,
            Opplæringspenger,
            Pleiepenger,
            OppgittAndreYtelserUtenforNav,
            AndreØkonomiskeYtelser,
        )
        testPerson.løsBehov(Sykepenger, true)
        testPerson.løsBehov("Sykepenger dagsats", 200.0)
        testPerson.løsBehov(
            Sykepenger,
            Omsorgspenger,
            Svangerskapspenger,
            Foreldrepenger,
            Opplæringspenger,
            Pleiepenger,
            OppgittAndreYtelserUtenforNav,
            AndreØkonomiskeYtelser,
        )
    }

    private fun antallOpplysninger() =
        personRepository.hent(ident.tilPersonIdentfikator())?.let {
            it.behandlinger().size shouldBe 1
            it.behandlinger().flatMap { behandling -> behandling.opplysninger().finnAlle() }.size
        }

    private fun aktiveOpplysninger() =
        personRepository.hent(ident.tilPersonIdentfikator())?.let {
            it.behandlinger().size shouldBe 1
            it
                .behandlinger()
                .flatMap { behandling -> behandling.opplysninger().finnAlle() }
                .joinToString("\n") { it.opplysningstype.navn }
        }

    private fun godkjennOpplysninger(fase: String) {
        Approvals.verify(aktiveOpplysninger(), Approvals.NAMES.withParameters(fase))
    }

    private val Person.aktivBehandling get() = this.behandlinger().first()

    private val Behandling.aktivAvklaringer get() = this.aktiveAvklaringer()

    private fun Meldingsinnhold.opptjeningsperiodeEr(måneder: Int) {
        val periode =
            Period.between(medDato(OpptjeningsperiodeFraOgMed), medDato(SisteAvsluttendeKalenderMåned)) + Period.ofMonths(1)
        withClue("Opptjeningsperiode skal være 3 år") { periode.toTotalMonths() shouldBe måneder }
    }

    private class TestSaksbehandler(
        private val testPerson: TestPerson,
        private val hendelseMediator: HendelseMediator,
        private val personRepository: PersonRepository,
        private val rapid: TestRapid,
    ) {
        fun beslutt() {
            hendelseMediator.behandle(
                BesluttBehandlingHendelse(
                    meldingsreferanseId = UUIDv7.ny(),
                    ident = testPerson.ident,
                    behandlingId = testPerson.behandlingId.toUUID(),
                    opprettet = LocalDateTime.now(),
                    besluttetAv = Saksbehandler("NAV987987"),
                ),
                rapid,
            )
        }

        fun godkjenn() {
            hendelseMediator.behandle(
                GodkjennBehandlingHendelse(
                    meldingsreferanseId = UUIDv7.ny(),
                    ident = testPerson.ident,
                    behandlingId = testPerson.behandlingId.toUUID(),
                    godkjentAv = Saksbehandler("NAV123123"),
                    opprettet = LocalDateTime.now(),
                ),
                rapid,
            )
        }

        fun sendTilbake() {
            hendelseMediator.behandle(
                SendTilbakeHendelse(
                    meldingsreferanseId = UUIDv7.ny(),
                    ident = testPerson.ident,
                    behandlingId = testPerson.behandlingId.toUUID(),
                    opprettet = LocalDateTime.now(),
                ),
                rapid,
            )
        }

        fun lukkAlleAvklaringer() {
            val behandling = personRepository.hent(testPerson.ident.tilPersonIdentfikator())?.behandlinger()?.first()
            behandling.shouldNotBeNull()
            val avklaringer: List<Avklaring> = behandling.aktiveAvklaringer()

            avklaringer.forEach { avklaring ->
                hendelseMediator.behandle(
                    AvklaringKvittertHendelse(
                        meldingsreferanseId = UUIDv7.ny(),
                        ident = testPerson.ident,
                        avklaringId = avklaring.id,
                        behandlingId = behandling.behandlingId,
                        saksbehandler = "NAV123123",
                        begrunnelse = "",
                        opprettet = LocalDateTime.now(),
                    ),
                    rapid,
                )
            }
        }
    }

    private class TestObservatør : PersonObservatør {
        val tilstandsendringer = mutableListOf<BehandlingEndretTilstand>()

        override fun endretTilstand(event: BehandlingEndretTilstand) {
            tilstandsendringer.add(event)
        }
    }

    private fun åpneAvklaringer(): Map<String, String> {
        val behandling = personRepository.hent(ident.tilPersonIdentfikator())?.behandlinger()?.first()
        behandling.shouldNotBeNull()
        return behandling.aktiveAvklaringer().associate { it.id.toString() to it.kode.kode }
    }

    private fun TestRapid.harAvklaring(
        avklaringkode: Avklaringkode,
        block: Meldingsinnhold.() -> Unit,
    ) {
        val melding = finnAvklaringMelding(avklaringkode)
        block(melding)
    }

    private fun TestRapid.finnAvklaringMelding(avklaringkode: Avklaringkode): Meldingsinnhold {
        for (i in inspektør.size - 1 downTo 0 step 1) {
            val message = inspektør.message(i)
            if (message["@event_name"].asText() == "NyAvklaring") {
                if (message["kode"].asText() == avklaringkode.kode) {
                    return Meldingsinnhold(message)
                }
            }
        }

        throw IllegalStateException("Fant ikke avklaring med kode $avklaringkode")
    }

    private fun TestRapid.harHendelse(
        navn: String,
        offset: Int = 1,
        block: Meldingsinnhold.() -> Unit = {},
    ) {
        val message = inspektør.message(inspektør.size - offset)
        withClue("Siste melding på rapiden skal inneholde hendelse: $navn") {
            message["@event_name"].asText() shouldBe navn
        }
        Meldingsinnhold(message).apply { block() }
    }

    private fun TestRapid.harBehov(
        vararg behov: String,
        melding: Int = 1,
    ) {
        withClue("Siste melding på rapiden skal inneholde behov: ${behov.toList()}") {
            val sisteMelding = inspektør.message(inspektør.size - melding)
            assert(
                sisteMelding["@event_name"].asText() == "behov",
            ) {
                "Forventet behov '${
                    behov.joinToString {
                        it
                    }
                }' men siste melding er ikke et behov. Siste melding er ${sisteMelding["@event_name"].asText()}."
            }
            sisteMelding["@behov"].map { it.asText() } shouldContainAll behov.toList()
        }
    }

    private fun TestRapid.harBehov(
        behov: String,
        block: Meldingsinnhold.() -> Unit,
    ) {
        harBehov(behov)
        Meldingsinnhold(inspektør.message(inspektør.size - 1)[behov]).apply { block() }
    }

    private fun TestRapid.harFelt(block: Meldingsinnhold.() -> Unit) {
        Meldingsinnhold(inspektør.message(inspektør.size - 1)).apply { block() }
    }

    private class Meldingsinnhold(
        private val message: JsonNode,
    ) {
        fun medNode(navn: String) = message.get(navn)

        fun medFastsettelser(block: Fastsettelser.() -> Unit) {
            Fastsettelser(medNode("fastsatt")).apply { block() }
        }

        fun medMeldingsInnhold(
            navn: String,
            block: Meldingsinnhold.() -> Unit,
        ) = Meldingsinnhold(message.get(navn)).apply { block() }

        fun medTekst(navn: String) = message.get(navn)?.asText()

        fun medDato(navn: String) = message.get(navn)?.asLocalDate()

        fun medTimestamp(navn: String) = message.get(navn)?.asLocalDateTime()

        fun medBoolsk(navn: String) = message.get(navn)?.asBoolean()

        fun medOpplysning(navn: String) = message.get("opplysninger").single { it["navn"].asText() == navn }

        fun medVilkår(
            navn: String,
            block: Vilkår.() -> Unit,
        ) = Vilkår(
            message.get("vilkår").single {
                it["navn"].asText() == navn
            },
        ).apply { block() }

        inline fun <reified T> medOpplysning(navn: String): T =
            when (T::class) {
                Boolean::class -> medOpplysning(navn)["verdi"].asBoolean() as T
                String::class -> medOpplysning(navn)["verdi"].asText() as T
                LocalDate::class -> medOpplysning(navn)["verdi"].asLocalDate() as T
                Int::class -> medOpplysning(navn)["verdi"].asInt() as T
                else -> throw IllegalArgumentException("Ukjent type")
            }

        inner class Vilkår(
            private val jsonNode: JsonNode,
        ) {
            private val status = jsonNode["status"].asText()
            private val navn = jsonNode["navn"].asText()

            fun erOppfylt() = withClue("$navn skal være oppfylt") { status shouldBe "Oppfylt" }

            fun erIkkeOppfylt() = withClue("$navn skal være ikke oppfylt") { status shouldBe "IkkeOppfylt" }

            fun hjemmel() = jsonNode["hjemmel"].asText()
        }

        inner class Fastsettelser(
            private val jsonNode: JsonNode,
        ) {
            private val utfall = jsonNode["utfall"].asBoolean()

            val status get() = jsonNode["status"].asText()

            val grunnlag get() = jsonNode["grunnlag"]["grunnlag"].asInt()
            val vanligArbeidstidPerUke get() = jsonNode["fastsattVanligArbeidstid"]["vanligArbeidstidPerUke"].asDouble()
            val sats get() = jsonNode["sats"]["dagsatsMedBarnetillegg"].asInt()

            fun periode(navn: String) = jsonNode["kvoter"].find { it["navn"].asText() == navn }?.get("verdi")?.asInt()

            val samordning get() = jsonNode["samordning"]

            val oppfylt get() = withClue("Utfall skal være true") { utfall shouldBe true }

            val `ikke oppfylt` get() = withClue("Utfall skal være false") { utfall shouldBe false }
        }
    }
}
