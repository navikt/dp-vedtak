package no.nav.dagpenger.vedtak.iverksetting.mediator

import no.nav.dagpenger.vedtak.juni
import java.time.LocalDateTime
import java.util.UUID

internal fun dagpengerInnvilgetHendelse(vedtakId: UUID, behandlingId: UUID, ident: String, sakId: String) =
    // language=JSON
    """
    {
                  "@event_name": "dagpenger_innvilget",
                  "@id": "ACC7FDCF-4F21-4960-BE67-6591CFD2731D",
                  "@opprettet": "${LocalDateTime.now()}",
                  "ident": "$ident",
                  "behandlingId": "$behandlingId",
                  "sakId": "$sakId",
                  "vedtakId": "$vedtakId",
                  "vedtaktidspunkt": "${LocalDateTime.MAX}",
                  "virkningsdato": "2019-08-24"
    }
    """.trimIndent()

internal fun dagpengerAvslåttHendelse(vedtakId: UUID, behandlingId: UUID, ident: String, sakId: String) =
    // language=JSON
    """
    {
                  "@event_name": "dagpenger_avslått",
                  "@id": "ACC7FDCF-4F21-4960-BE67-6591CFD2731D",
                  "@opprettet": "${LocalDateTime.now()}",
                  "ident": "$ident",
                  "behandlingId": "$behandlingId",
                  "sakId": "$sakId",
                  "vedtakId": "$vedtakId",
                  "vedtaktidspunkt": "${LocalDateTime.MAX}",
                  "virkningsdato": "2019-08-24"
    }
    """.trimIndent()
internal fun utbetalingVedtakFattet(ident: String, vedtakId: UUID, behandlingId: UUID, sakId: String) =
    //language=JSON
    """
        {
          "@event_name": "utbetaling_vedtak_fattet",
          "ident": "$ident",
          "behandlingId": "$behandlingId",
          "sakId": "$sakId",
          "vedtakId": "$vedtakId",
          "vedtaktidspunkt": "${LocalDateTime.MAX}",
          "virkningsdato": "${11 juni 2023}",
          "utbetalingsdager": [
            {
              "dato": "2023-05-29",
              "beløp": "0.0"
            },
            {
              "dato": "2023-05-30",
              "beløp": "0.0"
            },
            {
              "dato": "2023-05-31",
              "beløp": "0.0"
            },
            {
              "dato": "2023-06-01",
              "beløp": "0.0"
            },
            {
              "dato": "2023-06-02",
              "beløp": "0.0"
            },
            {
              "dato": "2023-06-05",
              "beløp": "0.0"
            },
            {
              "dato": "2023-06-06",
              "beløp": "0.0"
            },
            {
              "dato": "2023-06-07",
              "beløp": "0.0"
            },
            {
              "dato": "2023-06-08",
              "beløp": "0.0"
            },
            {
              "dato": "2023-06-09",
              "beløp": "0.0"
            }
          ],
          "utfall": "Innvilget",
          "@id": "418a136f-196b-45fe-8c45-76730d88ebd5",
          "@opprettet": "2023-06-15T19:24:58.050467",
          "system_read_count": 0,
          "system_participating_services": [
            {
              "id": "418a136f-196b-45fe-8c45-76730d88ebd5",
              "time": "2023-06-15T19:24:58.050467"
            }
          ]
        }
    """.trimIndent()
