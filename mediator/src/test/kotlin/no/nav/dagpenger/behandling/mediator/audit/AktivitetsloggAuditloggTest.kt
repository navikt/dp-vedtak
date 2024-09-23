package no.nav.dagpenger.behandling.mediator.audit

import io.kotest.matchers.shouldBe
import no.nav.dagpenger.behandling.mediator.AktivitetsloggMediator
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.Test

class AktivitetsloggAuditloggTest {
    private val rapid = TestRapid()
    private val aktivitetsloggMediator = AktivitetsloggMediator(rapid)

    @Test
    fun `publiserer auditlogg som aktivitetslogg på rapiden`() {
        val auditlogg = AktivitetsloggAuditlogg(aktivitetsloggMediator)
        val ident = "12345678901"
        auditlogg.opprett("melding", ident, "Z123456")

        rapid.inspektør.size shouldBe 1

        with(rapid.inspektør.message(0)) {
            get("@event_name").asText() shouldBe "aktivitetslogg"
            get("ident").asText() shouldBe ident
            get("hendelse")["type"].asText() shouldBe "Auditlogg"
            get("aktiviteter").size() shouldBe 1
        }
    }
}
