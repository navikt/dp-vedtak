package no.nav.dagpenger.behandling

import io.kotest.matchers.shouldBe
import no.nav.dagpenger.behandling.modell.BehandlingObservatør
import no.nav.dagpenger.behandling.modell.UUIDv7
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import kotlin.test.Test

class KafkaBehandlingObservatørTest {
    private val testRapid = TestRapid()
    private val observatør = KafkaBehandlingObservatør(testRapid)

    @Test
    fun `skal sende behandling_opprettet til kafka`() {
        val behandlingId = UUIDv7.ny()
        val søknadId = UUIDv7.ny()
        observatør.behandlingOpprettet(
            BehandlingObservatør.BehandlingOpprettet(
                ident = "12345678910",
                behandlingId = behandlingId,
                søknadId = søknadId,
            ),
        )
        testRapid.inspektør.message(0).also { jsonEvent ->
            jsonEvent["@event_name"].asText().shouldBe("behandling_opprettet")
            jsonEvent["ident"].asText().shouldBe("12345678910")
            jsonEvent["behandlingId"].asText().shouldBe(behandlingId.toString())
            jsonEvent["søknadId"].asText().shouldBe(søknadId.toString())
        }
    }
}
