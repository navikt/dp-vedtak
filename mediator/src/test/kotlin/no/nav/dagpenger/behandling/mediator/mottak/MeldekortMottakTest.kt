package no.nav.dagpenger.behandling.mediator.mottak

import com.github.navikt.tbd_libs.rapids_and_rivers.test_support.TestRapid
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import no.nav.dagpenger.behandling.mediator.MessageMediator
import no.nav.dagpenger.behandling.modell.hendelser.AktivitetType
import no.nav.dagpenger.behandling.modell.hendelser.MeldekortHendelse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import kotlin.time.Duration.Companion.hours

class MeldekortMottakTest {
    private val rapid = TestRapid()
    private val messageMediator = mockk<MessageMediator>(relaxed = true)

    init {
        MeldekortMottak(rapid, messageMediator)
    }

    @BeforeEach
    fun setup() {
        rapid.reset()
        clearMocks(messageMediator)
    }

    @Test
    fun `vi kan ta i mot et meldekort`() {
        rapid.sendTestMessage(json)
        val hendelse = slot<MeldekortHendelse>()

        verify {
            messageMediator.behandle(capture(hendelse), any(), any())
        }

        hendelse.isCaptured shouldBe true
        hendelse.captured.ident() shouldBe "12345123451"
        hendelse.captured.fom shouldBe LocalDate.of(2025, 1, 20)
        hendelse.captured.tom shouldBe LocalDate.of(2025, 2, 2)
        hendelse.captured.dager.size shouldBe 14
        hendelse.captured.dager
            .first()
            .dato shouldBe LocalDate.of(2025, 1, 20)
        hendelse.captured.dager
            .first()
            .aktiviteter.size shouldBe 1
        hendelse.captured.dager
            .first()
            .aktiviteter
            .first()
            .type shouldBe AktivitetType.Arbeid
        hendelse.captured.dager
            .first()
            .aktiviteter
            .first()
            .tid shouldBe 5.hours
    }
}

val json =
    """
    {
      "@event_name": "rapporteringsperiode_innsendt_hendelse",
      "ident": "12345123451",
      "rapporteringsId": "64dc9ae2-68d7-4df3-8dbc-cace10241394",
      "fom": "2025-01-20",
      "tom": "2025-02-02",
      "kilde": {
        "rolle": "Bruker",
        "ident": "12345123451"
      },
      "dager": [
        {
          "dato": "2025-01-20",
          "aktiviteter": [
            {
              "type": "Arbeid",
              "tid": "PT5H"
            }
          ]
        },
        {
          "dato": "2025-01-21",
          "aktiviteter": []
        },
        {
          "dato": "2025-01-22",
          "aktiviteter": [
            {
              "type": "Ferie",
              "tid": "PT24H"
            }
          ]
        },
        {
          "dato": "2025-01-23",
          "aktiviteter": [
            {
              "type": "Syk",
              "tid": "PT24H"
            }
          ]
        },
        {
          "dato": "2025-01-24",
          "aktiviteter": [
            {
              "type": "Arbeid",
              "tid": "PT2H"
            }
          ]
        },
        {
          "dato": "2025-01-25",
          "aktiviteter": []
        },
        {
          "dato": "2025-01-26",
          "aktiviteter": []
        },
        {
          "dato": "2025-01-27",
          "aktiviteter": []
        },
        {
          "dato": "2025-01-28",
          "aktiviteter": []
        },
        {
          "dato": "2025-01-29",
          "aktiviteter": []
        },
        {
          "dato": "2025-01-30",
          "aktiviteter": []
        },
        {
          "dato": "2025-01-31",
          "aktiviteter": []
        },
        {
          "dato": "2025-02-01",
          "aktiviteter": []
        },
        {
          "dato": "2025-02-02",
          "aktiviteter": []
        }
      ],
      "@id": "c1e95eca-cc53-4c58-aa16-957f1e623f74",
      "@opprettet": "2023-06-12T08:40:44.544584",
      "system_read_count": 0,
      "system_participating_services": [
        {
          "id": "c1e95eca-cc53-4c58-aa16-957f1e623f74",
          "time": "2023-06-12T08:40:44.544584"
        }
      ]
    }
    """.trimIndent()
