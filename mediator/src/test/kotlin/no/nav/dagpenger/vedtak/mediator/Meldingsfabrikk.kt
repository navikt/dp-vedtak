package no.nav.dagpenger.vedtak.mediator

import java.time.LocalDate
import java.util.UUID

object Meldingsfabrikk {

    fun dagpengerInnvilgetJson(
        rettighetstype: String = "Ordinær",
        ident: String = "12345123451",
        virkningsdato: LocalDate = LocalDate.now(),
    ) =
        //language=JSON
        """
        {        
          "@event_name": "søknad_behandlet_hendelse",
          "ident" : "$ident",
          "behandlingId": "${UUID.randomUUID()}",
          "Virkningsdato": "$virkningsdato",
          "innvilget": true,
          "Rettighetstype": "$rettighetstype",
          "Dagsats": "500",
          "Grunnlag": "500000",
          "Periode": "52",
          "Fastsatt vanlig arbeidstid": "8",
          "egenandel": "1500",
          "barnetillegg" : [ {
              "fødselsdato" : "2012-03-03"
          }, {
              "fødselsdato" : "2015-03-03"
          }]
           
        } 
        """.trimIndent()

    fun dagpengerAvslåttJson(ident: String = "12345123451") =
        //language=JSON
        """
        {        
          "@event_name": "søknad_behandlet_hendelse",
          "ident" : "$ident",
          "behandlingId": "${UUID.randomUUID()}",
          "Virkningsdato": "${LocalDate.now()}",
          "innvilget": false
        } 
        """.trimIndent()

    fun rapportering2(ident: String = "12345123451") =
        //language=JSON
        """
        
        {
          "@event_name": "rapporteringsperiode_innsendt_hendelse",
          "ident": "$ident",
          "rapporteringsId": "5e5dc83c-33fd-409d-92f2-513790c72e23",
          "fom": "2023-05-29",
          "tom": "2023-06-11",
          "dager": [
            {
              "dato": "2023-05-29",
              "aktiviteter": [
                {
                  "type": "Arbeid",
                  "tid": "PT1H"
                }
              ]
            },
            {
              "dato": "2023-05-30",
              "aktiviteter": [
                {
                  "type": "Arbeid",
                  "tid": "PT1H"
                }
              ]
            },
            {
              "dato": "2023-05-31",
              "aktiviteter": [
                {
                  "type": "Arbeid",
                  "tid": "PT1H"
                }
              ]
            },
            {
              "dato": "2023-06-01",
              "aktiviteter": [
                {
                  "type": "Arbeid",
                  "tid": "PT1H"
                }
              ]
            },
            {
              "dato": "2023-06-02",
              "aktiviteter": [
                {
                  "type": "Arbeid",
                  "tid": "PT1H"
                }
              ]
            },
            {
              "dato": "2023-06-03",
              "aktiviteter": [
                {
                  "type": "Arbeid",
                  "tid": "PT0H"
                }
              ]
            },
            {
              "dato": "2023-06-04",
              "aktiviteter": [
                {
                  "type": "Arbeid",
                  "tid": "PT0H"
                }
              ]
            },
            {
              "dato": "2023-06-05",
              "aktiviteter": [
                {
                  "type": "Arbeid",
                  "tid": "PT1H"
                }
              ]
            },
            {
              "dato": "2023-06-06",
              "aktiviteter": [
                {
                  "type": "Arbeid",
                  "tid": "PT1H"
                }
              ]
            },
            {
              "dato": "2023-06-07",
              "aktiviteter": [
                {
                  "type": "Arbeid",
                  "tid": "PT1H"
                }
              ]
            },
            {
              "dato": "2023-06-08",
              "aktiviteter": [
                {
                  "type": "Arbeid",
                  "tid": "PT1H"
                }
              ]
            },
            {
              "dato": "2023-06-09",
              "aktiviteter": [
                {
                  "type": "Arbeid",
                  "tid": "PT1H"
                }
              ]
            },
            {
              "dato": "2023-06-10",
              "aktiviteter": [
                {
                  "type": "Arbeid",
                  "tid": "PT0H"
                }
              ]
            },
            {
              "dato": "2023-06-11",
              "aktiviteter": [
                {
                  "type": "Arbeid",
                  "tid": "PT0H"
                }
              ]
            }
          ],
          "@id": "d05a24a2-bb59-40ab-a3bb-789c2999b4c3",
          "@opprettet": "2023-06-13T13:49:43.957357",
          "system_read_count": 0,
          "system_participating_services": [
            {
              "id": "d05a24a2-bb59-40ab-a3bb-789c2999b4c3",
              "time": "2023-06-13T13:49:43.957357"
            }
          ]
        }
        
        """.trimIndent()
    fun rapporteringInnsendtJson(ident: String = "12345123451", fom: LocalDate = LocalDate.now()) =
        //language=JSON
        """
{
	"@event_name": "rapporteringsperiode_innsendt_hendelse",
	"ident" : "$ident",
	"rapporteringsId": "64dc9ae2-68d7-4df3-8dbc-cace10241394",
	"fom": "$fom",
	"tom": "${fom.plusDays(13)}",
	"dager": [
		{
			"dato": "$fom",
			"aktiviteter": [
				{
					"type": "Arbeid",
					"tid": "PT5H"
				}
			]
		},
		{
			"dato": "${fom.plusDays(1)}",
			"aktiviteter": []
		},
		{
			"dato": "${fom.plusDays(2)}",
			"aktiviteter": [
				{
					"type": "Ferie",
					"tid": "PT24H"
				}
			]
		},
		{
			"dato": "${fom.plusDays(3)}",
			"aktiviteter": [
				{
					"type": "Syk",
					"tid": "PT24H"
				}
			]
		},
		{
			"dato": "${fom.plusDays(4)}",
			"aktiviteter": [
				{
					"type": "Arbeid",
					"tid": "PT2H"
				}
			]
		},
		{
			"dato": "${fom.plusDays(5)}",
			"aktiviteter": []
		},
		{
			"dato": "${fom.plusDays(6)}",
			"aktiviteter": []
		},
		{
			"dato": "${fom.plusDays(7)}",
			"aktiviteter": []
		},
		{
			"dato": "${fom.plusDays(8)}",
			"aktiviteter": []
		},
		{
			"dato": "${fom.plusDays(9)}",
			"aktiviteter": []
		},
		{
			"dato": "${fom.plusDays(10)}",
			"aktiviteter": []
		},
		{
			"dato": "${fom.plusDays(11)}",
			"aktiviteter": []
		},
		{
			"dato": "${fom.plusDays(12)}",
			"aktiviteter": []
		},
		{
			"dato": "${fom.plusDays(13)}",
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

    fun iverksettJson(
        vedtakId: UUID = UUID.fromString("408f11d9-4be8-450a-8b7a-c2f3f9811859"),
        virkningsdato: LocalDate = LocalDate.of(2023, 6, 11),
    ) =
        //language=JSON
        """{
        "@event_name": "behov",
        "@behovId": "fe6fb8ee-cbc7-46bf-a5d7-fb9b57b279c4",
        "@behov": [
          "Iverksett"
        ],
        "ident": "12345678911",
        "iverksettingId": "0b853210-cc2b-45d8-9c35-72b39fa1d7f3",
        "vedtakId": "$vedtakId",
        "tilstand": "Mottatt",
        "Iverksett": {
          "vedtakId": "408f11d9-4be8-450a-8b7a-c2f3f9811859",
          "behandlingId": "0aaa66b9-35c2-4398-aca0-d1d0a9465292",
          "vedtakstidspunkt": "2019-08-24T14:15:22",
          "virkningsdato": "$virkningsdato",
          "utbetalingsdager": [
            {"dato": "${virkningsdato.minusDays(13)}", "beløp": "800"},
            {"dato": "${virkningsdato.minusDays(12)}", "beløp": "800"},
            {"dato": "${virkningsdato.minusDays(11)}", "beløp": "800"},
            {"dato": "${virkningsdato.minusDays(10)}", "beløp": "800"},
            {"dato": "${virkningsdato.minusDays(9)}", "beløp": "800"},
            {"dato": "${virkningsdato.minusDays(6)}", "beløp": "800"},
            {"dato": "${virkningsdato.minusDays(5)}", "beløp": "800"},
            {"dato": "${virkningsdato.minusDays(4)}", "beløp": "800"},
            {"dato": "${virkningsdato.minusDays(3)}", "beløp": "800"},
            {"dato": "${virkningsdato.minusDays(2)}", "beløp": "800"}
          ],
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
}
