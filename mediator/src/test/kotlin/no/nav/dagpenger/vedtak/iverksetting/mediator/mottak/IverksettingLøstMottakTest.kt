package no.nav.dagpenger.vedtak.iverksetting.mediator.mottak

import io.kotest.assertions.assertSoftly
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import no.nav.dagpenger.vedtak.iverksetting.hendelser.IverksattHendelse
import no.nav.dagpenger.vedtak.mediator.IHendelseMediator
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.Test
import java.util.UUID

internal class IverksettingLøstMottakTest {

    private val testRapid = TestRapid()
    private val iHendelseMediator = mockk<IHendelseMediator>()
    private val iverksettingLøstMottak = IverksettingLøstMottak(testRapid, iHendelseMediator)
    private val testIdent = "12345678910"

    @Test
    fun `Tar imot løste iverksettingsbehov`() {
        val iverksattHendelse = slot<IverksattHendelse>()
        every { iHendelseMediator.behandle(capture(iverksattHendelse), any(), any()) } just Runs
        testRapid.sendTestMessage(løsningForIverksettingJson())

        verify(exactly = 1) {
            iHendelseMediator.behandle(any<IverksattHendelse>(), any(), any())
        }

        assertSoftly {
            iverksattHendelse.isCaptured shouldBe true
            val captured = iverksattHendelse.captured
            captured.ident() shouldBe testIdent
            captured.vedtakId shouldBe UUID.fromString("408f11d9-4be8-450a-8b7a-c2f3f9811859")
            captured.iverksettingId shouldBe UUID.fromString("0b853210-cc2b-45d8-9c35-72b39fa1d7f3")
        }
    }

    private fun løsningForIverksettingJson() = //language=JSON
        """
            {
              "@event_name": "behov",
              "@behovId": "fe6fb8ee-cbc7-46bf-a5d7-fb9b57b279c4",
              "@behov": [
                "Iverksett"
              ],
              "ident": "$testIdent",
              "iverksettingId": "0b853210-cc2b-45d8-9c35-72b39fa1d7f3",
              "vedtakId": "408f11d9-4be8-450a-8b7a-c2f3f9811859",
              "tilstand": "Mottatt",
              "Iverksett": {
                "vedtakId": "408f11d9-4be8-450a-8b7a-c2f3f9811859",
                "behandlingId": "0aaa66b9-35c2-4398-aca0-d1d0a9465292",
                "sakId": "SAK_NUMMER_1",
                "vedtakstidspunkt": "2019-08-24T14:15:22",
                "virkningsdato": "2019-08-24",
                "utfall": "Innvilget"
              },
              "behandlingId": "0aaa66b9-35c2-4398-aca0-d1d0a9465292",
              "sakId": "SAK_NUMMER_1",
              "vedtakstidspunkt": "2019-08-24T14:15:22",
              "virkningsdato": "2019-08-24",
              "utfall": "Innvilget",
              "@id": "2a49bcc2-2101-435d-83cc-2cc7905041b9",
              "@opprettet": "2023-05-11T10:02:10.0279828",
              "system_read_count": 1,
              "system_participating_services": [
                {
                  "id": "2a49bcc2-2101-435d-83cc-2cc7905041b9",
                  "time": "2023-05-11T10:02:10.027982800"
                },
                {
                  "id": "2a49bcc2-2101-435d-83cc-2cc7905041b9",
                  "time": "2023-05-12T09:42:16.481322"
                }
              ],
              "@løsning": {
                "Iverksett": true
              }
            }
        """.trimIndent()
}
