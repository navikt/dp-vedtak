package no.nav.dagpenger.vedtak.mediator

import no.nav.dagpenger.vedtak.juni
import no.nav.dagpenger.vedtak.mai
import java.time.LocalDate
import java.util.*
import kotlin.time.Duration

object Meldingsfabrikk {

    fun rettighetBehandletOgInnvilgetJson(
        rettighetstype: String = "Ordinær",
        ident: String = "12345123451",
        sakId: String = "SAK_NUMMER_1",
        virkningsdato: LocalDate = LocalDate.now(),
        dagsats: Double = 500.0,
        fastsattVanligArbeidstid: Int = 8,
        meldingId: UUID = UUID.randomUUID(),
    ) =
        //language=JSON
        """
        {        
          "@event_name": "rettighet_behandlet_hendelse",
          "@id": "$meldingId",
          "ident" : "$ident",
          "sakId" : "$sakId",
          "behandlingId": "${UUID.randomUUID()}",
          "Virkningsdato": "$virkningsdato",
          "utfall": "Innvilgelse",
          "Rettighetstype": "$rettighetstype",
          "Dagsats": "$dagsats",
          "Grunnlag": "500000",
          "Periode": "52",
          "Fastsatt vanlig arbeidstid": "$fastsattVanligArbeidstid",
          "barnetillegg" : [ {
              "fødselsdato" : "2012-03-03"
          }, {
              "fødselsdato" : "2015-03-03"
          }]
           
        } 
        """.trimIndent()

    fun rettighetBehandletOgAvslåttJson(
        rettighetstype: String = "Ordinær",
        ident: String = "12345123451",
        sakId: String = "SAK_NUMMER_1",
    ) =
        //language=JSON
        """
        {        
          "@event_name": "rettighet_behandlet_hendelse",
          "ident" : "$ident",
          "sakId" : "$sakId",
          "behandlingId": "${UUID.randomUUID()}",
          "Virkningsdato": "${LocalDate.now()}",
          "utfall": "Avslag",
          "Rettighetstype": "$rettighetstype"
        } 
        """.trimIndent()

    fun rapporteringInnsendtJson(
        ident: String = "12345123451",
        fom: LocalDate = 29 mai 2023,
        tom: LocalDate = 11 juni 2023,
        tidArbeidetPerArbeidsdag: Duration = Duration.ZERO,
    ): String {
        val arbeidstid = tidArbeidetPerArbeidsdag.toIsoString()
        //language=JSON
        return """
            {
              "@event_name": "rapporteringsperiode_innsendt_hendelse",
              "ident": "$ident",
              "rapporteringsId": "5e5dc83c-33fd-409d-92f2-513790c72e23",
              "fom": "$fom",
              "tom": "$tom",
              "dager": [
                {
                  "dato": "$fom",
                  "aktiviteter": [
                    {
                      "type": "Arbeid",
                      "tid": "$arbeidstid"
                    }
                  ]
                },
                {
                  "dato": "${fom.plusDays(1)}",
                  "aktiviteter": [
                    {
                      "type": "Arbeid",
                      "tid": "$arbeidstid"
                    }
                  ]
                },
                {
                  "dato": "${fom.plusDays(2)}",
                  "aktiviteter": [
                    {
                      "type": "Arbeid",
                      "tid": "$arbeidstid"
                    }
                  ]
                },
                {
                  "dato": "${fom.plusDays(3)}",
                  "aktiviteter": [
                    {
                      "type": "Arbeid",
                      "tid": "$arbeidstid"
                    }
                  ]
                },
                {
                  "dato": "${fom.plusDays(4)}",
                  "aktiviteter": [
                    {
                      "type": "Arbeid",
                      "tid": "$arbeidstid"
                    }
                  ]
                },
                {
                  "dato": "${fom.plusDays(5)}",
                  "aktiviteter": [
                    {
                      "type": "Arbeid",
                      "tid": "PT0S"
                    }
                  ]
                },
                {
                  "dato": "${fom.plusDays(6)}",
                  "aktiviteter": [
                    {
                      "type": "Arbeid",
                      "tid": "PT0S"
                    }
                  ]
                },
                {
                  "dato": "${fom.plusDays(7)}",
                  "aktiviteter": [
                    {
                      "type": "Arbeid",
                      "tid": "$arbeidstid"
                    }
                  ]
                },
                {
                  "dato": "${fom.plusDays(8)}",
                  "aktiviteter": [
                    {
                      "type": "Arbeid",
                      "tid": "$arbeidstid"
                    }
                  ]
                },
                {
                  "dato": "${fom.plusDays(9)}",
                  "aktiviteter": [
                    {
                      "type": "Arbeid",
                      "tid": "$arbeidstid"
                    }
                  ]
                },
                {
                  "dato": "${fom.plusDays(10)}",
                  "aktiviteter": [
                    {
                      "type": "Arbeid",
                      "tid": "$arbeidstid"
                    }
                  ]
                },
                {
                  "dato": "${fom.plusDays(11)}",
                  "aktiviteter": [
                    {
                      "type": "Arbeid",
                      "tid": "$arbeidstid"
                    }
                  ]
                },
                {
                  "dato": "${fom.plusDays(12)}",
                  "aktiviteter": [
                    {
                      "type": "Arbeid",
                      "tid": "PT0S"
                    }
                  ]
                },
                {
                  "dato": "${fom.plusDays(13)}",
                  "aktiviteter": [
                    {
                      "type": "Arbeid",
                      "tid": "PT0S"
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
    }

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

    fun lagRapporteringForMeldeperiodeFørDagpengvedtaket(ident: String, dagpengerFraDato: LocalDate): String {
        // language=json
        return """
            {
              "@event_name": "rapporteringsperiode_innsendt_hendelse",
              "ident": "$ident",
              "rapporteringsId": "5e5dc83c-33fd-409d-92f2-513790c72e23",
              "fom": "${dagpengerFraDato.minusDays(14)}",
              "tom": "${dagpengerFraDato.minusDays(1)}",
              "dager": [
                {
                  "dato": "${dagpengerFraDato.minusDays(14)}",
                  "aktiviteter": [
                    {
                      "type": "Arbeid",
                      "tid": "PT0H"
                    }
                  ]
                },
                {
                  "dato": "${dagpengerFraDato.minusDays(13)}",
                  "aktiviteter": [
                    {
                      "type": "Arbeid",
                      "tid": "PT0H"
                    }
                  ]
                },
                {
                  "dato": "${dagpengerFraDato.minusDays(12)}",
                  "aktiviteter": [
                    {
                      "type": "Arbeid",
                      "tid": "PT0H"
                    }
                  ]
                },
                {
                  "dato": "${dagpengerFraDato.minusDays(11)}",
                  "aktiviteter": [
                    {
                      "type": "Arbeid",
                      "tid": "PT0H"
                    }
                  ]
                },
                {
                  "dato": "${dagpengerFraDato.minusDays(10)}",
                  "aktiviteter": [
                    {
                      "type": "Arbeid",
                      "tid": "PT0H"
                    }
                  ]
                },
                {
                  "dato": "${dagpengerFraDato.minusDays(9)}",
                  "aktiviteter": [
                    {
                      "type": "Arbeid",
                      "tid": "PT0H"
                    }
                  ]
                },
                {
                  "dato": "${dagpengerFraDato.minusDays(8)}",
                  "aktiviteter": [
                    {
                      "type": "Arbeid",
                      "tid": "PT0H"
                    }
                  ]
                },
                {
                  "dato": "${dagpengerFraDato.minusDays(7)}",
                  "aktiviteter": [
                    {
                      "type": "Arbeid",
                      "tid": "PT0H"
                    }
                  ]
                },
                {
                  "dato": "${dagpengerFraDato.minusDays(6)}",
                  "aktiviteter": [
                    {
                      "type": "Arbeid",
                      "tid": "PT0H"
                    }
                  ]
                },
                {
                  "dato": "${dagpengerFraDato.minusDays(5)}",
                  "aktiviteter": [
                    {
                      "type": "Arbeid",
                      "tid": "PT0H"
                    }
                  ]
                },
                {
                  "dato": "${dagpengerFraDato.minusDays(4)}",
                  "aktiviteter": [
                    {
                      "type": "Arbeid",
                      "tid": "PT0H"
                    }
                  ]
                },
                {
                  "dato": "${dagpengerFraDato.minusDays(3)}",
                  "aktiviteter": [
                    {
                      "type": "Arbeid",
                      "tid": "PT0H"
                    }
                  ]
                },
                {
                  "dato": "${dagpengerFraDato.minusDays(2)}",
                  "aktiviteter": [
                    {
                      "type": "Arbeid",
                      "tid": "PT0H"
                    }
                  ]
                },
                {
                  "dato": "${dagpengerFraDato.minusDays(1)}",
                  "aktiviteter": [
                    {
                      "type": "Arbeid",
                      "tid": "PT0H"
                    }
                  ]
                }
              ],
              "@id": "d05a24a2-bb59-40ab-a3bb-789c2999b4c3",
              "@opprettet": "2023-06-16T10:11:07.397317679",
              "system_read_count": 2,
              "system_participating_services": [
                {
                  "id": "d05a24a2-bb59-40ab-a3bb-789c2999b4c3",
                  "time": "2023-06-13T13:49:43.957357"
                },
                {
                  "id": "d05a24a2-bb59-40ab-a3bb-789c2999b4c3",
                  "time": "2023-06-16T10:11:07.396525607",
                  "service": "dp-quizmaster",
                  "instance": "dp-quizmaster-b5d4cd8b5-pcs4p",
                  "image": "docker.pkg.github.com/navikt/dp-quizmaster/dp-quizmaster:c67861e66772fbc190274e3613d1f588c96e8eb1"
                },
                {
                  "id": "d05a24a2-bb59-40ab-a3bb-789c2999b4c3",
                  "time": "2023-06-20T12:12:37.078119837",
                  "service": "dp-vedtak",
                  "instance": "dp-vedtak-54469d4d6b-jb8f9",
                  "image": "europe-north1-docker.pkg.dev/nais-management-233d/teamdagpenger/dp-vedtak:2023.06.19-11.10-0c163f8"
                }
              ]
            }
        """.trimIndent()
    }
}
