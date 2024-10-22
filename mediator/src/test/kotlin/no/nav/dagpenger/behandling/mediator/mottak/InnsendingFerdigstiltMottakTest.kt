package no.nav.dagpenger.behandling.mediator.mottak

import com.github.navikt.tbd_libs.rapids_and_rivers.test_support.TestRapid
import io.kotest.matchers.shouldBe
import no.nav.dagpenger.behandling.mediator.asUUID
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import java.util.UUID

class InnsendingFerdigstiltMottakTest {
    private val rapid =
        TestRapid().also {
            InnsendingFerdigstiltMottak(it)
        }
    private val ident = "123123123"
    private val søknadId by lazy { UUID.randomUUID() }

    @Test
    fun `tar imot innsending og republiserer som behandlingsklar`() {
        rapid.sendTestMessage(innsendingJSON)
        rapid.inspektør.size shouldBe 1
        rapid.inspektør.key(0) shouldBe ident
        with(rapid.inspektør.message(0)) {
            this["ident"].asText() shouldBe ident
            this["fagsakId"].asInt() shouldBe 123
            this["journalpostId"].asInt() shouldBe 123
            this["søknadId"].asUUID() shouldBe søknadId
        }
    }

    @Language("JSON")
    val innsendingJSON =
        """
        {
          "@event_name": "innsending_ferdigstilt",
          "type": "NySøknad",
          "fødselsnummer": "$ident",
          "fagsakId": "123",
          "søknadsData": {
            "søknad_uuid": "$søknadId"
          },
          "journalpostId": "123"
        }
        """.trimIndent()
}
