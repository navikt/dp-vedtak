package no.nav.dagpenger.behandling.mediator.audit

import com.github.navikt.tbd_libs.rapids_and_rivers.test_support.TestRapid
import io.kotest.matchers.shouldBe
import no.nav.dagpenger.behandling.mediator.AktivitetsloggMediator
import org.junit.jupiter.api.Test

class ApiAuditloggTest {
    private val rapid = TestRapid()
    private val aktivitetsloggMediator = AktivitetsloggMediator()

    @Test
    fun `publiserer auditlogg som aktivitetslogg på rapiden`() {
        val auditlogg = ApiAuditlogg(aktivitetsloggMediator, rapid)
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
