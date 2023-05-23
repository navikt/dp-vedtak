package no.nav.dagpenger.vedtak.mediator

import com.fasterxml.jackson.databind.JsonNode
import io.mockk.mockk
import no.nav.dagpenger.aktivitetslogg.Aktivitet
import no.nav.dagpenger.aktivitetslogg.Aktivitetskontekst
import no.nav.dagpenger.aktivitetslogg.Aktivitetslogg
import no.nav.dagpenger.aktivitetslogg.SpesifikkKontekst
import no.nav.dagpenger.vedtak.modell.VedtakBehov
import no.nav.dagpenger.vedtak.modell.hendelser.Hendelse
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime
import java.util.UUID

internal class BehovMediatorTest {
    private companion object {
        private const val testIdent = "12345678912"
        private val iverksettingId = UUID.fromString("4AF61401-CE9D-4FD0-AD25-8A85DA8F3BF8")
        private lateinit var behovMediator: BehovMediator
    }

    private val testRapid = TestRapid()
    private lateinit var aktivitetslogg: Aktivitetslogg
    private lateinit var iverksattKontekst: IverksattKontekst

    @BeforeEach
    fun setup() {
        aktivitetslogg = Aktivitetslogg()
        iverksattKontekst = IverksattKontekst(iverksettingId, testIdent)
        behovMediator = BehovMediator(
            rapidsConnection = testRapid,
            sikkerLogg = mockk(relaxed = true),
        )
        testRapid.reset()
    }

    @Test
    internal fun `Behov blir sendt og inneholder det den skal`() {
        val hendelse = TestHendelse(aktivitetslogg.barn())
        hendelse.kontekst(iverksattKontekst)
        hendelse.kontekst(Testkontekst("Testkontekst"))

        hendelse.behov(
            VedtakBehov.Iverksett,
            "Behøver iverksetting",
            mapOf(
                "parameter1" to "verdi1",
                "parameter2" to "verdi2",
            ),
        )

        behovMediator.håndter(hendelse)

        val inspektør = testRapid.inspektør

        assertEquals(1, inspektør.size)
        assertEquals(testIdent, inspektør.key(0), "Forventer at partisjonsnøkker er ident ($testIdent)")
        inspektør.message(0).also { json ->
            assertStandardBehovFelter(json)
            assertEquals(listOf("Iverksett"), json["@behov"].map(JsonNode::asText))
            assertEquals(testIdent, json["ident"].asText())
            assertEquals("Testkontekst", json["Testkontekst"].asText())
            assertEquals("verdi1", json["parameter1"].asText())
            assertEquals("verdi2", json["parameter2"].asText())
            assertEquals("verdi1", json["Iverksett"]["parameter1"].asText())
            assertEquals("verdi2", json["Iverksett"]["parameter2"].asText())
        }
    }

    private enum class TestBehov : Aktivitet.Behov.Behovtype {
        Test,
    }

    @Test
    internal fun `Gruppere behov`() {
        val hendelse = TestHendelse(aktivitetslogg.barn())
        hendelse.kontekst(iverksattKontekst)
        hendelse.kontekst(Testkontekst("Testkontekst"))

        hendelse.behov(
            TestBehov.Test,
            "Test",
            mapOf(
                "parameter1" to "verdi1",
                "parameter2" to "verdi2",
            ),
        )

        hendelse.behov(
            VedtakBehov.Iverksett,
            "Behøver iverksetting",
            mapOf(
                "parameter3" to "verdi3",
                "parameter4" to "verdi4",
            ),
        )

        behovMediator.håndter(hendelse)

        val inspektør = testRapid.inspektør

        assertEquals(1, inspektør.size)
        inspektør.message(0).also { json ->
            println(json)
            assertStandardBehovFelter(json)
            assertEquals(listOf("Test", "Iverksett"), json["@behov"].map(JsonNode::asText))
            // assertEquals(testIdent, json["ident"].asText())
            assertEquals("Testkontekst", json["Testkontekst"].asText())
            assertEquals("verdi1", json["parameter1"].asText())
            assertEquals("verdi2", json["parameter2"].asText())
            assertEquals("verdi3", json["parameter3"].asText())
            assertEquals("verdi4", json["parameter4"].asText())
            assertEquals("verdi1", json["Test"]["parameter1"].asText())
            assertEquals("verdi2", json["Test"]["parameter2"].asText())
            assertEquals("verdi3", json["Iverksett"]["parameter3"].asText())
            assertEquals("verdi4", json["Iverksett"]["parameter4"].asText())
        }
    }

    @Test
    internal fun `sjekker etter duplikatverdier`() {
        val hendelse = TestHendelse(aktivitetslogg.barn())
        hendelse.kontekst(iverksattKontekst)
        hendelse.behov(
            VedtakBehov.Iverksett,
            "Behøver iverksetting",
            mapOf(
                "ident" to testIdent,
            ),
        )
        hendelse.behov(
            VedtakBehov.Iverksett,
            "Behøver iverksetting",
            mapOf(
                "ident" to testIdent,
            ),
        )

        assertThrows<IllegalArgumentException> { behovMediator.håndter(hendelse) }
    }

    @Test
    internal fun `kan ikke produsere samme behov`() {
        val hendelse = TestHendelse(aktivitetslogg.barn())
        hendelse.kontekst(iverksattKontekst)
        hendelse.behov(VedtakBehov.Iverksett, "Behøver iverksetting")
        hendelse.behov(VedtakBehov.Iverksett, "Behøver iverksetting")

        assertThrows<IllegalArgumentException> { behovMediator.håndter(hendelse) }
    }

    private fun assertStandardBehovFelter(json: JsonNode) {
        assertEquals("behov", json["@event_name"].asText())
        assertTrue(json.hasNonNull("@id"))
        assertDoesNotThrow { UUID.fromString(json["@id"].asText()) }
        assertTrue(json.hasNonNull("@opprettet"))
        assertDoesNotThrow { LocalDateTime.parse(json["@opprettet"].asText()) }
    }

    private class Testkontekst(
        private val melding: String,
    ) : Aktivitetskontekst {
        override fun toSpesifikkKontekst() = SpesifikkKontekst(melding, mapOf(melding to melding))
    }

    private class TestHendelse(
        val logg: Aktivitetslogg,
    ) : Hendelse(testIdent, logg), Aktivitetskontekst {
        init {
            logg.kontekst(this)
        }

        override fun toSpesifikkKontekst() = SpesifikkKontekst("TestHendelse")
        override fun kontekstMap(): Map<String, String> = emptyMap()

        override fun kontekst(kontekst: Aktivitetskontekst) {
            logg.kontekst(kontekst)
        }
    }

    private class IverksattKontekst(private val iverksettId: UUID, private val ident: String) : Aktivitetskontekst {
        override fun toSpesifikkKontekst() =
            SpesifikkKontekst(kontekstType = "Iverksett", mapOf("iverksettId" to iverksettId.toString(), "ident" to ident))
    }
}
