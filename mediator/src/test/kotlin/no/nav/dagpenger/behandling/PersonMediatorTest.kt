package no.nav.dagpenger.behandling

import com.fasterxml.jackson.databind.JsonNode
import io.kotest.assertions.withClue
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import no.nav.dagpenger.behandling.db.Postgres.withMigratedDb
import no.nav.dagpenger.behandling.mediator.BehovMediator
import no.nav.dagpenger.behandling.mediator.HendelseMediator
import no.nav.dagpenger.behandling.mediator.MessageMediator
import no.nav.dagpenger.behandling.mediator.PersonMediator
import no.nav.dagpenger.behandling.mediator.melding.PostgresHendelseRepository
import no.nav.dagpenger.behandling.mediator.repository.BehandlingRepositoryPostgres
import no.nav.dagpenger.behandling.mediator.repository.OpplysningerRepositoryPostgres
import no.nav.dagpenger.behandling.mediator.repository.PersonRepositoryPostgres
import no.nav.dagpenger.behandling.modell.Ident.Companion.tilPersonIdentfikator
import no.nav.dagpenger.behandling.modell.UUIDv7
import no.nav.dagpenger.behandling.modell.hendelser.ForslagGodkjentHendelse
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

    private val personRepository = PersonRepositoryPostgres(BehandlingRepositoryPostgres(OpplysningerRepositoryPostgres()))

    init {
        MessageMediator(
            rapidsConnection = rapid,
            personMediator =
                PersonMediator(
                    personRepository = personRepository,
                    aktivitetsloggMediator = mockk(relaxed = true),
                    behovMediator = BehovMediator(rapid),
                    hendelseMediator = HendelseMediator(rapid),
                    observatører = emptySet(),
                ),
            hendelseRepository = PostgresHendelseRepository(),
        )
    }

    @BeforeEach
    fun setUp() {
        rapid.reset()
    }

    @Test
    fun `e2e av søknad innsendt`() =
        withMigratedDb {
            val testPerson =
                TestPerson(
                    ident,
                    rapid,
                    søknadstidspunkt = 5.mai(2021),
                )
            testPerson.sendSøknad()
            rapid.harHendelse("behandling_opprettet", offset = 2)

            rapid.harBehov("Søknadstidspunkt") {
                medTekst("søknad_uuid") shouldBe testPerson.søknadId
                medNode("InnsendtSøknadsId")["urn"].asText() shouldBe "urn:soknad:${testPerson.søknadId}"
            }
            rapid.harBehov("Fødselsdato", "Søknadstidspunkt", "ØnskerDagpengerFraDato")
            testPerson.løsBehov("Fødselsdato", "Søknadstidspunkt", "ØnskerDagpengerFraDato")

            rapid.harBehov(InntektId) {
                medDato(SisteAvsluttendeKalenderMåned) shouldBe 31.mars(2021)
                medDato(OpptjeningsperiodeFraOgMed) shouldBe 1.mars(2018)
                opptjeningsperiodeEr(måneder = 36)
            }
            testPerson.løsBehov(InntektId)

            rapid.harBehov("InntektSiste12Mnd") { medTekst("InntektId") shouldBe testPerson.inntektId }
            rapid.harBehov("InntektSiste36Mnd") { medTekst("InntektId") shouldBe testPerson.inntektId }

            testPerson.løsBehov("InntektSiste12Mnd", "InntektSiste36Mnd")

            rapid.harHendelse("forslag_til_vedtak") {
                medBoolsk("utfall") shouldBe false
            }

            personRepository.hent(ident.tilPersonIdentfikator()).also {
                it.shouldNotBeNull()
                it.behandlinger().size shouldBe 1
                it.behandlinger().flatMap { behandling -> behandling.opplysninger().finnAlle() }.size shouldBe 25

                // Godkjenner forslag til vedtak
                // TODO: Skal dette også være mulig å gjøre via rapiden?
                it.behandlinger().first().let { behandling ->
                    behandling.håndter(ForslagGodkjentHendelse(UUIDv7.ny(), ident, behandling.behandlingId))
                }
            }

            /*
            // TODO: Dette funker bare om hendelsen har gått via mediatoren
            rapid.harHendelse("vedtak_fattet") {
                medBoolsk("utfall") shouldBe false
            }*/
        }

    private fun Meldingsinnhold.opptjeningsperiodeEr(måneder: Int) {
        val periode = Period.between(medDato(OpptjeningsperiodeFraOgMed), medDato(SisteAvsluttendeKalenderMåned))
        withClue("Opptjeningsperiode skal være 3 år") { periode.toTotalMonths() shouldBe måneder }
    }
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

private class Meldingsinnhold(private val message: JsonNode) {
    fun medNode(navn: String) = message.get(navn)

    fun medTekst(navn: String) = message.get(navn)?.asText()

    fun medDato(navn: String) = message.get(navn)?.asLocalDate()

    fun medBoolsk(navn: String) = message.get(navn)?.asBoolean()
}
