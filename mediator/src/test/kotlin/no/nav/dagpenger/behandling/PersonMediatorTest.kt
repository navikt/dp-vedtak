package no.nav.dagpenger.behandling

import com.fasterxml.jackson.databind.JsonNode
import com.github.navikt.tbd_libs.rapids_and_rivers.asLocalDate
import com.github.navikt.tbd_libs.rapids_and_rivers.asLocalDateTime
import com.github.navikt.tbd_libs.rapids_and_rivers.test_support.TestRapid
import io.kotest.assertions.withClue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldHaveSingleElement
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import no.nav.dagpenger.avklaring.Avklaringkode
import no.nav.dagpenger.behandling.db.Postgres.withMigratedDb
import no.nav.dagpenger.behandling.konfigurasjon.Feature
import no.nav.dagpenger.behandling.konfigurasjon.skruAvFeatures
import no.nav.dagpenger.behandling.konfigurasjon.skruPåFeature
import no.nav.dagpenger.behandling.mediator.BehovMediator
import no.nav.dagpenger.behandling.mediator.HendelseMediator
import no.nav.dagpenger.behandling.mediator.MessageMediator
import no.nav.dagpenger.behandling.mediator.melding.PostgresHendelseRepository
import no.nav.dagpenger.behandling.mediator.repository.AvklaringKafkaObservatør
import no.nav.dagpenger.behandling.mediator.repository.AvklaringRepositoryPostgres
import no.nav.dagpenger.behandling.mediator.repository.BehandlingRepositoryPostgres
import no.nav.dagpenger.behandling.mediator.repository.OpplysningerRepositoryPostgres
import no.nav.dagpenger.behandling.mediator.repository.PersonRepositoryPostgres
import no.nav.dagpenger.behandling.modell.Behandling
import no.nav.dagpenger.behandling.modell.Behandling.TilstandType.UnderOpprettelse
import no.nav.dagpenger.behandling.modell.BehandlingObservatør.BehandlingEndretTilstand
import no.nav.dagpenger.behandling.modell.Ident.Companion.tilPersonIdentfikator
import no.nav.dagpenger.behandling.modell.Person
import no.nav.dagpenger.behandling.modell.PersonObservatør
import no.nav.dagpenger.opplysning.Saksbehandlerkilde
import no.nav.dagpenger.regel.Avklaringspunkter
import no.nav.dagpenger.regel.Behov.HelseTilAlleTyperJobb
import no.nav.dagpenger.regel.Behov.Inntekt
import no.nav.dagpenger.regel.Behov.InntektId
import no.nav.dagpenger.regel.Behov.KanJobbeDeltid
import no.nav.dagpenger.regel.Behov.KanJobbeHvorSomHelst
import no.nav.dagpenger.regel.Behov.OpptjeningsperiodeFraOgMed
import no.nav.dagpenger.regel.Behov.RegistrertSomArbeidssøker
import no.nav.dagpenger.regel.Behov.SisteAvsluttendeKalenderMåned
import no.nav.dagpenger.regel.Behov.TarUtdanningEllerOpplæring
import no.nav.dagpenger.regel.Behov.Verneplikt
import no.nav.dagpenger.regel.Behov.VilligTilÅBytteYrke
import no.nav.dagpenger.regel.RegelverkDagpenger
import no.nav.dagpenger.regel.TapAvArbeidsinntektOgArbeidstid
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.LocalDate
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

    private val forventetAntallOpplysningerInnvilgelse = 76
    private val forventetAntallOpplysningerAvslag = 47
    private val forventetAntallOpplysningerKnockout = 29

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
                    søknadstidspunkt = 6.mai(2021),
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
    fun `søknad med for lite inntekt skal automatisk avslås`() =
        withMigratedDb {
            skruPåFeature(Feature.INNVILGELSE)
            val testPerson =
                TestPerson(
                    ident,
                    rapid,
                    søknadstidspunkt = 6.mai(2021),
                )
            løsBehandlingFramTilFerdig(testPerson)

            personRepository.hent(ident.tilPersonIdentfikator()).also {
                it.shouldNotBeNull()
                it.behandlinger().size shouldBe 1
                it.behandlinger().flatMap { behandling -> behandling.opplysninger().finnAlle() }.size shouldBe
                    forventetAntallOpplysningerAvslag

                it.aktivBehandling.aktivAvklaringer.shouldHaveSize(6)
                it.behandlinger().flatMap { behandling -> behandling.opplysninger().finnAlle() }.size shouldBe
                    forventetAntallOpplysningerAvslag
            }

            listOf(
                Avklaringspunkter.EØSArbeid,
                Avklaringspunkter.HattLukkedeSakerSiste8Uker,
                Avklaringspunkter.InntektNesteKalendermåned,
                Avklaringspunkter.JobbetUtenforNorge,
                Avklaringspunkter.MuligGjenopptak,
                Avklaringspunkter.SvangerskapsrelaterteSykepenger,
            ).forEach { avklaringkode: Avklaringkode ->
                rapid.harAvklaring(avklaringkode) {
                    val avklaringId = medTekst("avklaringId")!!
                    testPerson.markerAvklaringIkkeRelevant(avklaringId, avklaringkode.kode)
                }
            }

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
                medVilkår("Krav til minsteinntekt") {
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

            rapid.inspektør.size shouldBe 25

            testObservatør.tilstandsendringer.size shouldBe 3

            repeat(rapid.inspektør.size) {
                // @todo: Fjerne hendelse fra aktivitetslogg og bruk observatør for alle hendelser ut.
                val forventerNøkkelFor = setOf("vedtak_fattet", "behandling_endret_tilstand")
                withClue("Melding nr $it skal ha nøkkel. Meldingsinnhold: ${rapid.inspektør.message(it)}") {
                    if (rapid.inspektør.message(it)["@event_name"].asText() in forventerNøkkelFor) {
                        rapid.inspektør.key(it) shouldBe ident
                    }
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
                    søknadstidspunkt = 6.mai(2021),
                    InntektSiste12Mnd = 500000,
                )
            løsBehandlingFramTilMinsteinntekt(testPerson)

            personRepository.hent(ident.tilPersonIdentfikator()).also {
                it.shouldNotBeNull()
                it.behandlinger().size shouldBe 1
                it
                    .behandlinger()
                    .flatMap { behandling -> behandling.opplysninger().finnAlle() }
                    .size shouldBe forventetAntallOpplysningerKnockout
            }

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
                    søknadstidspunkt = 6.mai(2021),
                    InntektSiste12Mnd = 500000,
                )
            skruPåFeature(Feature.INNVILGELSE)
            løsBehandlingFramTilFerdig(testPerson)

            personRepository.hent(ident.tilPersonIdentfikator()).also {
                it.shouldNotBeNull()
                it.behandlinger().size shouldBe 1
                it
                    .behandlinger()
                    .flatMap { behandling -> behandling.opplysninger().finnAlle() }
                    .size shouldBe forventetAntallOpplysningerInnvilgelse
            }
            /**
             * Innhenter tar utdanning eller opplæring
             */
            rapid.harBehov(TarUtdanningEllerOpplæring)
            testPerson.løsBehov(TarUtdanningEllerOpplæring)

            /**
             * Innhenter inntekt for fastsettelse
             */
            rapid.harBehov(Inntekt)
            testPerson.løsBehov(Inntekt)

            rapid.harHendelse("forslag_til_vedtak") {
                medBoolsk("utfall") shouldBe true
            }

            // TODO: Beregningsmetode for tapt arbeidstid har defaultverdi for testing av innvilgelse og derfor mangler avklaringen
            rapid.inspektør.size shouldBe 20

            rapid.harAvklaring(Avklaringspunkter.Totrinnskontroll) {}

            rapid.harHendelse("forslag_til_vedtak") {
                medBoolsk("utfall") shouldBe true
            }

            testPerson.godkjennForslagTilVedtak()

            rapid.harHendelse("vedtak_fattet") {
                medFastsattelser {
                    oppfylt
                    withClue("Grunnlag bør større enn 0") { grunnlag shouldBeGreaterThan 0 }
                    vanligArbeidstidPerUke shouldBe 37.5
                    sats shouldBeGreaterThan 0
                }
            }
        }

    @Test
    fun `søknad som slår ut på manuelle behandling må føre til forslag til vedtak`() =
        withMigratedDb {
            val testPerson =
                TestPerson(
                    ident,
                    rapid,
                    søknadstidspunkt = 6.mai(2021),
                )

            løsBehandlingFramTilFerdig(testPerson)

            rapid.harHendelse("forslag_til_vedtak") {
                medTekst("søknadId") shouldBe testPerson.søknadId
                with(medNode("avklaringer")) {
                    this.size() shouldBe 6
                    val avklaring = this.first()
                    avklaring["type"].asText() shouldBe Avklaringspunkter.HattLukkedeSakerSiste8Uker.kode
                    avklaring["begrunnelse"].asText() shouldBe Avklaringspunkter.HattLukkedeSakerSiste8Uker.beskrivelse
                    avklaring["utfall"].asText() shouldBe "Manuell"
                }
            }

            rapid.inspektør.size shouldBe
                listOf(
                    "opprettet" to 1,
                    "behov" to 7,
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
                    søknadstidspunkt = 6.mai(2021),
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
                    søknadstidspunkt = 5.juni(2024),
                    innsendt = 5.juni(2024).atTime(12, 0),
                    ønskerFraDato = 10.juni(2024),
                )
            løsBehandlingFramTilFerdig(testPerson)

            rapid.harHendelse("forslag_til_vedtak") {
                with(medNode("avklaringer")) {
                    this.size() shouldBe 7
                    this.shouldHaveSingleElement { it["type"].asText() == Avklaringspunkter.ØnskerEtterRapporteringsfrist.kode }
                }
            }
        }

    @Test
    fun `søker etter overgang til ny rapporteringsfrist`() =
        withMigratedDb {
            val testPerson =
                TestPerson(
                    ident,
                    rapid,
                    søknadstidspunkt = 7.juni(2024),
                    innsendt = 7.juni(2024).atTime(12, 0),
                    ønskerFraDato = 10.juni(2024),
                )
            løsBehandlingFramTilFerdig(testPerson)

            rapid.harHendelse("forslag_til_vedtak") {
                with(medNode("avklaringer")) {
                    this.size() shouldBe 6
                }
            }
        }

    @Test
    fun `søker for langt fram i tid`() =
        withMigratedDb {
            val testPerson =
                TestPerson(
                    ident,
                    rapid,
                    innsendt = 1.juni(2024).atTime(12, 0),
                    søknadstidspunkt = 1.juni(2024),
                    ønskerFraDato = 30.juni(2024),
                )
            løsBehandlingFramTilFerdig(testPerson)

            rapid.harHendelse("forslag_til_vedtak") {
                with(medNode("avklaringer")) {
                    this.size() shouldBe 8
                }
            }
        }

    @Test
    fun `redigering av opplysning i forslag til vedtak`() {
        withMigratedDb {
            val testPerson =
                TestPerson(
                    ident,
                    rapid,
                    innsendt = 1.juni(2024).atTime(12, 0),
                    søknadstidspunkt = 1.juni(2024),
                    ønskerFraDato = 30.juni(2024),
                )
            løsBehandlingFramTilFerdig(testPerson)

            rapid.harHendelse("forslag_til_vedtak") {
                with(medNode("avklaringer")) {
                    this.size() shouldBe 8
                }
            }

            testPerson.ønskerFraDato = 1.juli(2024)
            testPerson.løsBehov("ØnskerDagpengerFraDato")

            rapid.harHendelse("forslag_til_vedtak") {
                with(medNode("avklaringer")) {
                    this.size() shouldBe 7
                }
            }
            testPerson.løsBehov(
                "Beregnet vanlig arbeidstid per uke før tap",
                mapOf(
                    "verdi" to 40.0,
                    "@kilde" to mapOf("saksbehandler" to "123"),
                ),
            )
            personRepository.hent(ident.tilPersonIdentfikator()).also { person ->
                person.shouldNotBeNull()
                person.behandlinger().first().opplysninger().finnOpplysning(TapAvArbeidsinntektOgArbeidstid.beregnetArbeidstid).let {
                    it.verdi shouldBe 40.0
                    it.kilde.shouldNotBeNull()
                    it.kilde!!::class shouldBe Saksbehandlerkilde::class
                    (it.kilde!! as Saksbehandlerkilde).ident shouldBe "123"
                }
            }
        }
    }

    @Test
    fun `kan ikke konkludere når opplysninger gjelder i framtiden`() {
        withMigratedDb {
            val testPerson =
                TestPerson(
                    ident,
                    rapid,
                    innsendt = 1.juni(2024).atTime(12, 0),
                    søknadstidspunkt = 1.juni(2024),
                    ønskerFraDato = 30.juni(2024),
                    registrertFraDato = 1.juli(2024),
                )
            løsBehandlingFramTilFerdig(testPerson)

            rapid.harHendelse("forslag_til_vedtak") {
                with(medNode("avklaringer")) {
                    this.size() shouldBe 8
                }
            }
        }
    }

    enum class Behandlingslengde {
        Alder,
        Minsteinntekt,
        KravPåDagpenger,
    }

    private fun løsBehandlingFramTilFerdig(testPerson: TestPerson) {
        løsBehandlingFramTil(testPerson, Behandlingslengde.KravPåDagpenger)
    }

    private fun løsBehandlingFramTilMinsteinntekt(testPerson: TestPerson) {
        løsBehandlingFramTil(testPerson, Behandlingslengde.Minsteinntekt)
    }

    private fun løsBehandlingFramTil(
        testPerson: TestPerson,
        behandlingslengde: Behandlingslengde,
    ) {
        testPerson.sendSøknad()
        rapid.harHendelse("behandling_opprettet", offset = 2)

        /**
         * Fastsetter søknadstidspunkt
         */
        rapid.harBehov("Søknadstidspunkt") {
            medTekst("søknadId") shouldBe testPerson.søknadId
            medTekst("søknad_uuid") shouldBe testPerson.søknadId
        }

        rapid.harBehov("Fødselsdato", "Søknadstidspunkt", "ØnskerDagpengerFraDato")
        testPerson.løsBehov("Fødselsdato", "Søknadstidspunkt", "ØnskerDagpengerFraDato")

        /**
         * Sjekker om mulig verneplikt
         */
        rapid.harBehov(Verneplikt)
        testPerson.løsBehov(Verneplikt)

        /**
         * Fastsetter opptjeningsperiode og inntekt. Pt brukes opptjeningsperiode generert fra dp-inntekt
         */
        rapid.harBehov(InntektId) {
            medDato("Virkningsdato") shouldBe maxOf(testPerson.søknadstidspunkt, testPerson.ønskerFraDato)
            /**
             * TODO: Vi må ta vekk opptjeningsperiode fra dp-inntekt og skive om måten dp-inntekt lagrer inntekt på beregningsdato
             * medDato(OpptjeningsperiodeFraOgMed) shouldBe 1.april(2018)
             * opptjeningsperiodeEr(måneder = 36)
             */
        }
        testPerson.løsBehov(InntektId)

        /**
         * Sjekker kravene til inntekt
         */
        rapid.harBehov("InntektSiste12Mnd") { medTekst("InntektId") shouldBe testPerson.inntektId }
        rapid.harBehov("InntektSiste36Mnd") { medTekst("InntektId") shouldBe testPerson.inntektId }

        testPerson.løsBehov("InntektSiste12Mnd", "InntektSiste36Mnd")

        if (behandlingslengde == Behandlingslengde.Minsteinntekt) {
            return
        }

        /**
         * Sjekker kravene til reell arbeidssøker
         */
        rapid.harBehov(KanJobbeDeltid, KanJobbeHvorSomHelst, HelseTilAlleTyperJobb, VilligTilÅBytteYrke)
        testPerson.løsBehov(KanJobbeDeltid, KanJobbeHvorSomHelst, HelseTilAlleTyperJobb, VilligTilÅBytteYrke)

        /**
         * Sjekker kravet til registrering som arbeidssøker
         */
        rapid.harBehov(RegistrertSomArbeidssøker)
        testPerson.løsBehov(RegistrertSomArbeidssøker)

        /**
         * Innhenter rettighetstype
         rapid.harBehov(Ordinær, Permittert, Lønnsgaranti, PermittertFiskeforedling)
         testPerson.løsBehov(Ordinær, Permittert, Lønnsgaranti, PermittertFiskeforedling)
         */
    }

    private val Person.aktivBehandling get() = this.behandlinger().first()

    private val Behandling.aktivAvklaringer get() = this.aktiveAvklaringer()

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

    private fun Meldingsinnhold.opptjeningsperiodeEr(måneder: Int) {
        val periode = Period.between(medDato(OpptjeningsperiodeFraOgMed), medDato(SisteAvsluttendeKalenderMåned)) + Period.ofMonths(1)
        withClue("Opptjeningsperiode skal være 3 år") { periode.toTotalMonths() shouldBe måneder }
    }

    private class TestObservatør : PersonObservatør {
        val tilstandsendringer = mutableListOf<BehandlingEndretTilstand>()

        override fun endretTilstand(event: BehandlingEndretTilstand) {
            tilstandsendringer.add(event)
        }
    }
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
        inspektør.message(inspektør.size - melding)["@behov"].map { it.asText() } shouldContainAll behov.toList()
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

    fun medFastsattelser(block: Fastsettelser.() -> Unit) {
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

    fun medOpplysning(navn: String) = message.get("opplysninger").single { it["opplysningstype"]["id"].asText() == navn }

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

        val oppfylt get() = withClue("Utfall skal være true") { utfall shouldBe true }

        val `ikke oppfylt` get() = withClue("Utfall skal være false") { utfall shouldBe false }
    }
}
