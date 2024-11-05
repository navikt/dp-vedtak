package no.nav.dagpenger.behandling.mediator.mottak

import com.github.navikt.tbd_libs.rapids_and_rivers.test_support.TestRapid
import io.mockk.spyk
import io.mockk.verify
import no.nav.dagpenger.behandling.db.Postgres.withMigratedDb
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test

class ArenaOppgaveMottakTest {
    private val sakRepository = spyk(SakRepository())

    private val rapid =
        TestRapid().apply {
            ArenaOppgaveMottak(this, sakRepository)
        }

    @Test
    fun `Leser inn oppgaver`() {
        withMigratedDb {
            rapid.sendTestMessage(meldingJSON)

            verify {
                sakRepository.finnBehandling(15102351)
            }
        }
    }

    @Language("JSON")
    private val meldingJSON = """{
      "table": "SIAMO.OPPGAVE_LOGG",
      "op_type": "U",
      "op_ts": "2024-11-05 12:12:46.000000",
      "current_ts": "2024-11-05 12:12:51.325004",
      "pos": "00000003570305543589",
      "after": {
        "OPPGAVE_LOGG_ID": 238763568,
        "TRANS_ID": "148.27.871874",
        "TASK_ID": 267868027,
        "DESCRIPTION": "ABBA",
        "PRIORITY": 1,
        "DUEDATE": "2024-10-03 00:00:00",
        "USERNAME": "BDB4416",
        "KONTOR": "4416",
        "FYLKESNR": 50,
        "OPPRETTET_DATO": "2024-10-03 09:47:32",
        "ER_UTLAND": "N",
        "REG_DATO": "2024-10-03 09:47:32",
        "REG_USER": "ARBLINJE",
        "MOD_DATO": "2024-11-05 11:58:03",
        "MOD_USER": "ABC416",
        "OBJEKT": "SAK",
        "OBJEKT_ID": "15102351",
        "SAK_ID": 15102351,
        "SAK_TYPE": "DAGP",
        "VEDTAK_ID": null,
        "VEDTAK_TYPE": null,
        "OPPGAVETYPE_ID": "cx139.107.13.1874bfe6b:e639a26b28:-8000",
        "OPPGAVETYPE_BESKRIVELSE": "Start vedtaksbehandling",
        "GSAK_FAGOMRADE": "DAG",
        "GSAK_PRIORITET": "HOY_DAG",
        "OPERASJON": "DEL",
        "ENDRET_AV": "BDB4416",
        "TIDSPUNKT": "2024-11-05 12:12:45",
        "PERSON_ID": 3931114
      },
      "@id": "e0e25785-9332-4e2c-9a0f-9e85dfc82c1c",
      "@opprettet": "2024-11-05T12:12:51.351838572",
      "system_read_count": 0,
      "system_participating_services": [
        {
          "id": "e0e25785-9332-4e2c-9a0f-9e85dfc82c1c",
          "time": "2024-11-05T12:12:51.351838572",
          "service": "dp-behandling",
          "instance": "dp-behandling-5799cbb788-4x5lt",
          "image": "europe-north1-docker.pkg.dev/nais-management-233d/teamdagpenger/dp-behandling:2024.11.05-11.10-c68b7eb"
        }
      ]
    }"""
}
