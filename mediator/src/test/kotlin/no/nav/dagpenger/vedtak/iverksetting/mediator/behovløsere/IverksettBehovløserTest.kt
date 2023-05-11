package no.nav.dagpenger.vedtak.iverksetting.mediator.behovløsere

import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class IverksettBehovløserTest {

    private val testRapid = TestRapid()
    init {

        IverksettBehovløser(testRapid)
    }

    @Test
    fun `tull`() {
        //language=JSON
        val iverksettJson = """{
  "@event_name": "behov",
  "@behovId": "fe6fb8ee-cbc7-46bf-a5d7-fb9b57b279c4",
  "@behov": [
    "Iverksett"
  ],
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
        testRapid.sendTestMessage(iverksettJson)
        assertEquals(1, testRapid.inspektør.size)
    }
}
