package no.nav.dagpenger.vedtak.iverksetting.mediator.behovløsere

import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import no.nav.dagpenger.vedtak.iverksetting.mediator.behovløsere.models.IverksettDagpengerdDto
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.Test

internal class IverksettBehovløserTest {

    private val testRapid = TestRapid()
    private val iverksettClient = mockk<IverksettClient>()
    init {
        IverksettBehovløser(testRapid, iverksettClient)
    }

    @Test
    fun `at vi kaller iverksett APIet og besvarer behovet 'Iverksett'`() {
        val iverksettDtoSlot = slot<IverksettDagpengerdDto>()
        coEvery { iverksettClient.iverksett(capture(iverksettDtoSlot)) } just Runs

        testRapid.sendTestMessage(iverksettJson)
        coVerify(exactly = 1) {
            iverksettClient.iverksett(any())
        }
        iverksettDtoSlot.isCaptured shouldBe true
        testRapid.inspektør.size shouldBe 1
        with(testRapid.inspektør.message(0)) {
            val løsning = this["@løsning"]["Iverksett"]
            løsning.asBoolean() shouldBe true
        }
    }

    //language=JSON
    private val iverksettJson = """{
        "@event_name": "behov",
        "@behovId": "fe6fb8ee-cbc7-46bf-a5d7-fb9b57b279c4",
        "@behov": [
          "Iverksett"
        ],
        "ident": "12345678911",
        "iverksettingId": "0b853210-cc2b-45d8-9c35-72b39fa1d7f3",
        "vedtakId": "408f11d9-4be8-450a-8b7a-c2f3f9811859",
        "tilstand": "Mottatt",
        "Iverksett": {
          "vedtakId": "408f11d9-4be8-450a-8b7a-c2f3f9811859",
          "behandlingId": "0aaa66b9-35c2-4398-aca0-d1d0a9465292",
          "vedtakstidspunkt": "2019-08-24T14:15:22",
          "virkningsdato": "2019-08-24",
          "utfall": "Innvilget"
        },
        "behandlingId": "0aaa66b9-35c2-4398-aca0-d1d0a9465292",
        "vedtakstidspunkt": "2019-08-24T14:15:22",
        "virkningsdato": "2019-08-24",
        "utfall": "Innvilget",
        "@id": "2a49bcc2-2101-435d-83cc-2cc7905041b9",
        "@opprettet": "2023-05-11T10:02:10.0279828",
        "system_read_count": 0,
        "system_participating_services": [
          {
            "id": "2a49bcc2-2101-435d-83cc-2cc7905041b9",
            "time": "2023-05-11T10:02:10.027982800"
          }
        ]
      }"""
}
