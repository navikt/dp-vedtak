package no.nav.dagpenger.vedtak.iverksetting.mediator

import java.util.UUID

internal fun fattetVedtakJsonHendelse(vedtakId: UUID = UUID.fromString("df5e6587-a3e3-407c-8202-02f9740a09b0")): String =
    // language=JSON
    """
                {
                  "@event_type": "vedtak_fattet",
                  "ident": "12345678910",
                  "behandlingId": "0AAA66B9-35C2-4398-ACA0-D1D0A9465292",
                  "vedtakId": "$vedtakId",
                  "vedtaktidspunkt": "2019-08-24T14:15:22",
                  "virkningsdato": "2019-08-24",
                  "utfall": "Innvilget"
                }
    """.trimIndent()
