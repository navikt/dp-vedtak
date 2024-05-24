package no.nav.dagpenger.behandling.mediator.mottak

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import no.nav.dagpenger.behandling.TestOpplysningstyper.boolsk
import no.nav.dagpenger.behandling.mediator.MessageMediator
import no.nav.dagpenger.behandling.modell.hendelser.OpplysningSvarHendelse
import no.nav.dagpenger.opplysning.Gyldighetsperiode
import no.nav.dagpenger.opplysning.UUIDv7
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate

class OpplysningSvarMottakTest {
    private val rapid = TestRapid()
    private val messageMediator = mockk<MessageMediator>(relaxed = true)

    private val opplysningstype = boolsk // må registrere opplysningstype først

    init {
        OpplysningSvarMottak(rapid, messageMediator)
    }

    @BeforeEach
    fun setup() {
        rapid.reset()
        clearMocks(messageMediator)
    }

    @Test
    fun `tillater svar for opplysning uten metadata`() {
        rapid.sendTestMessage(løsningUtenMetadata.toJson())
        val hendelse = slot<OpplysningSvarHendelse>()
        verify {
            messageMediator.behandle(capture(hendelse), any(), any())
        }

        hendelse.isCaptured shouldBe true
        hendelse.captured.behandlingId shouldBe behandlingId
        hendelse.captured.opplysninger shouldHaveSize 1
        hendelse.captured.opplysninger.first().verdi shouldBe true
        hendelse.captured.opplysninger.first().opplysning().verdi shouldBe true
        hendelse.captured.opplysninger.first().opplysning().gyldighetsperiode shouldBe Gyldighetsperiode()
    }

    @Test
    fun `tillater svar med opplysning med metadata med fom og tom`() {
        rapid.sendTestMessage(løsningMedMetadata(gyldigFraOgMed, gyldigTilOgMed).toJson())
        val hendelse = slot<OpplysningSvarHendelse>()
        verify {
            messageMediator.behandle(capture(hendelse), any(), any())
        }

        hendelse.isCaptured shouldBe true
        hendelse.captured.behandlingId shouldBe behandlingId
        hendelse.captured.opplysninger shouldHaveSize 1
        hendelse.captured.opplysninger.first().verdi shouldBe true
        hendelse.captured.opplysninger.first().opplysning().verdi shouldBe true
        hendelse.captured.opplysninger.first().opplysning().gyldighetsperiode shouldBe
            Gyldighetsperiode(gyldigFraOgMed, gyldigTilOgMed)
    }

    @Test
    fun `Kan ikke besvare opplysning en ikke kjenner til`() {
        rapid.sendTestMessage(
            løsningMedMetadata(gyldigFraOgMed, gyldigTilOgMed, "ukjentOpplysning").toJson(),
        )
        val hendelse = slot<OpplysningSvarHendelse>()
        verify(exactly = 1) {
            messageMediator.behandle(capture(hendelse), any(), any())
        }
        hendelse.isCaptured shouldBe true
        hendelse.captured.opplysninger.shouldBeEmpty()
    }

    @Test
    fun `tillater svar med opplysning med metadata med og uten tom`() {
        rapid.sendTestMessage(løsningMedMetadata(gyldigFraOgMed, null).toJson())
        val hendelse = slot<OpplysningSvarHendelse>()
        verify {
            messageMediator.behandle(capture(hendelse), any(), any())
        }

        hendelse.captured.opplysninger.first().opplysning().gyldighetsperiode shouldBe
            Gyldighetsperiode(gyldigFraOgMed, LocalDate.MAX)
    }

    @Test
    fun `tillater svar med opplysning med metadata med og uten fom`() {
        rapid.sendTestMessage(løsningMedMetadata(null, gyldigFraOgMed).toJson())
        val hendelse = slot<OpplysningSvarHendelse>()
        verify {
            messageMediator.behandle(capture(hendelse), any(), any())
        }

        hendelse.captured.opplysninger.first().opplysning().gyldighetsperiode shouldBe
            Gyldighetsperiode(LocalDate.MIN, gyldigFraOgMed)
    }

    private val behandlingId = UUIDv7.ny()
    private val konvolutt =
        mapOf(
            "ident" to "12345678901",
            "behandlingId" to behandlingId,
            "@final" to true,
            "@opplysningsbehov" to true,
            "@behovId" to behandlingId,
        )
    private val løsningUtenMetadata =
        JsonMessage.newNeed(
            listOf("boolsk"),
            konvolutt +
                mapOf(
                    "@løsning" to
                        mapOf(
                            "boolsk" to true,
                        ),
                ),
        )
    private val gyldigFraOgMed = LocalDate.now()
    private val gyldigTilOgMed = gyldigFraOgMed.plusDays(5)

    private fun løsningMedMetadata(
        gyldigFraOgMed: LocalDate?,
        gyldigTilOgMed: LocalDate?,
        opplysningstype: String = "boolsk",
    ): JsonMessage {
        return JsonMessage.newNeed(
            listOf(opplysningstype),
            konvolutt +
                mapOf(
                    "@løsning" to
                        mapOf(
                            opplysningstype to
                                mapOf(
                                    "verdi" to true,
                                ) +
                                mapOf(
                                    "gyldigFraOgMed" to gyldigFraOgMed?.toString(),
                                    "gyldigTilOgMed" to gyldigTilOgMed?.toString(),
                                ).filterValues { it != null },
                        ),
                ),
        )
    }
}
