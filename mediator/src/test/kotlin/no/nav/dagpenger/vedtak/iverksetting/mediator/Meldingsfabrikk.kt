package no.nav.dagpenger.vedtak.iverksetting.mediator

internal fun fattetVedtakJsonHendelse(): String =
    // language=JSON
    """
                {
                  "@event_type": "vedtak_fattet",
                  "ident": "string",
                  "behandlingId": "0AAA66B9-35C2-4398-ACA0-D1D0A9465292",
                  "vedtakId": "df5e6587-a3e3-407c-8202-02f9740a09b0",
                  "vedtaktidspunkt": "2019-08-24T14:15:22",
                  "virkningsdato": "2019-08-24",
                  "utfall": "Innvilget"
                }
    """.trimIndent()
