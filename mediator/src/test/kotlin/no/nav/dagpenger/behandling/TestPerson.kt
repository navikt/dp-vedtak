package no.nav.dagpenger.behandling

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers.test_support.TestRapid
import no.nav.dagpenger.inntekt.v1.KlassifisertInntektMåned
import no.nav.dagpenger.regel.Behov.Barnetillegg
import no.nav.dagpenger.regel.Behov.HelseTilAlleTyperJobb
import no.nav.dagpenger.regel.Behov.Inntekt
import no.nav.dagpenger.regel.Behov.InntektId
import no.nav.dagpenger.regel.Behov.KanJobbeDeltid
import no.nav.dagpenger.regel.Behov.KanJobbeHvorSomHelst
import no.nav.dagpenger.regel.Behov.Lønnsgaranti
import no.nav.dagpenger.regel.Behov.Ordinær
import no.nav.dagpenger.regel.Behov.Permittert
import no.nav.dagpenger.regel.Behov.PermittertFiskeforedling
import no.nav.dagpenger.regel.Behov.RegistrertSomArbeidssøker
import no.nav.dagpenger.regel.Behov.Søknadsdato
import no.nav.dagpenger.regel.Behov.TarUtdanningEllerOpplæring
import no.nav.dagpenger.regel.Behov.Verneplikt
import no.nav.dagpenger.regel.Behov.VilligTilÅBytteYrke
import no.nav.dagpenger.regel.Behov.ØnskerDagpengerFraDato
import org.intellij.lang.annotations.Language
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth

class TestPerson(
    private val ident: String,
    private val rapid: TestRapid,
    internal val søknadsdato: LocalDate = 5.mai(2021),
    val alder: Int = 30,
    private val innsendt: LocalDateTime = LocalDateTime.now(),
    var InntektSiste12Mnd: Int = 1234,
    val InntektSiste36Mnd: Int = 1234,
    internal var ønskerFraDato: LocalDate = søknadsdato,
    internal var prøvingsdato: LocalDate = søknadsdato,
) {
    val inntektId = "01HQTE3GBWCSVYH6S436DYFREN"
    internal val søknadId = "4afce924-6cb4-4ab4-a92b-fe91e24f31bf"
    private val behandlingId by lazy { rapid.inspektør.field(1, "behandlingId").asText() }

    fun sendSøknad() = rapid.sendTestMessage(søknadInnsendt(), ident)

    private fun søknadInnsendt() =
        JsonMessage
            .newMessage(
                "søknad_behandlingsklar",
                mapOf(
                    "innsendt" to innsendt,
                    "ident" to ident,
                    "fagsakId" to 123,
                    "bruk-dp-behandling" to true,
                    "søknadId" to søknadId,
                ),
            ).toJson()

    fun løsBehov(vararg behov: String) {
        val behovSomLøses = løsninger.filterKeys { it in behov }
        require(behovSomLøses.size == behov.size) { "Fant ikke løsning for alle behov: $behov" }
        rapid.sendTestMessage(løstBehov(behovSomLøses), ident)
    }

    fun endreOpplysning(
        behov: String,
        løsning: Any,
        data: Map<String, Any> = emptyMap(),
    ) {
        val message =
            JsonMessage
                .newMessage(
                    "behov",
                    mapOf(
                        "ident" to ident,
                        "behandlingId" to behandlingId,
                        "søknadId" to søknadId,
                        "@behov" to listOf(behov),
                        "@opplysningsbehov" to true,
                        "@final" to true,
                        "@løsning" to
                            mapOf(
                                behov to løsning,
                            ),
                    ) + data,
                ).toJson()

        rapid.sendTestMessage(message, ident)
    }

    fun løsBehov(
        behov: String,
        løsning: Any,
        data: Map<String, Any> = emptyMap(),
    ) {
        rapid.sendTestMessage(løstBehov(mapOf(behov to løsning), true, data), ident)
    }

    private fun løstBehov(
        løsninger: Map<String, Any>,
        opplysningsbehov: Boolean = true,
        data: Map<String, Any> = emptyMap(),
    ): String =
        rapid.inspektør.message(rapid.inspektør.size - 1).run {
            val lol = this as ObjectNode
            lol.put("@final", true)
            lol.putPOJO("@løsning", løsninger)
            objectMapper.writeValueAsString(lol)
        }

    private val objectMapper =
        jacksonObjectMapper()
            .registerModule(JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

    fun markerAvklaringIkkeRelevant(
        avklaringId: String,
        kode: String,
    ) {
        rapid.sendTestMessage(avklaringIkkeRelevant(avklaringId, kode), ident)
    }

    private fun avklaringIkkeRelevant(
        avklaringId: String,
        kode: String,
    ) = JsonMessage
        .newMessage(
            "AvklaringIkkeRelevant",
            mapOf(
                "ident" to ident,
                "behandlingId" to behandlingId,
                "avklaringId" to avklaringId,
                "kode" to kode,
            ),
        ).toJson()

    fun avbrytBehandling() {
        rapid.sendTestMessage(
            JsonMessage
                .newMessage(
                    "avbryt_behandling",
                    mapOf(
                        "behandlingId" to behandlingId,
                        "ident" to ident,
                    ),
                ).toJson(),
            ident,
        )
    }

    fun sendTilKontroll() {
        rapid.sendTestMessage(
            JsonMessage
                .newMessage(
                    "oppgave_sendt_til_kontroll",
                    mapOf(
                        "behandlingId" to behandlingId,
                        "ident" to ident,
                    ),
                ).toJson(),
            ident,
        )
    }

    fun returnerTilSaksbehandler() {
        rapid.sendTestMessage(
            JsonMessage
                .newMessage(
                    "oppgave_returnert_til_saksbehandling",
                    mapOf(
                        "behandlingId" to behandlingId,
                        "ident" to ident,
                    ),
                ).toJson(),
            ident,
        )
    }

    fun godkjennForslagTilVedtak() {
        rapid.sendTestMessage(
            JsonMessage
                .newMessage(
                    "godkjenn_behandling",
                    mapOf(
                        "behandlingId" to behandlingId,
                        "ident" to ident,
                    ),
                ).toJson(),
            ident,
        )
    }

    @Language("JSON")
    private val løsningPåInntekt =
        """
        {
          "inntektsId": "01J677GHJRC2H08Q55DASFD0XX",
          "inntektsListe": [
            {
              "årMåned": "${YearMonth.from(søknadsdato.minusMonths(2))}",
              "klassifiserteInntekter": [
                {
                  "beløp": 41600.0,
                  "inntektKlasse": "ARBEIDSINNTEKT"
                }
              ],
              "harAvvik": false
            },
            {
              "årMåned": "${YearMonth.from(søknadsdato.minusMonths(3))}",
              "klassifiserteInntekter": [
                {
                  "beløp": 403660.0,
                  "inntektKlasse": "ARBEIDSINNTEKT"
                }
              ],
              "harAvvik": false
            }
          ],
          "manueltRedigert": false,
          "sisteAvsluttendeKalenderMåned": "2024-07"
        }
        """.trimIndent()

    private val inntektV1 =
        no.nav.dagpenger.inntekt.v1.Inntekt(
            inntektsId = "01J677GHJRC2H08Q55DASFD0XX",
            inntektsListe =
                listOf(
                    KlassifisertInntektMåned(
                        årMåned = YearMonth.from(søknadsdato.minusMonths(2)),
                        klassifiserteInntekter =
                            listOf(
                                no.nav.dagpenger.inntekt.v1.KlassifisertInntekt(
                                    beløp = InntektSiste12Mnd.toBigDecimal(),
                                    inntektKlasse = no.nav.dagpenger.inntekt.v1.InntektKlasse.ARBEIDSINNTEKT,
                                ),
                            ),
                        harAvvik = false,
                    ),
                ),
            sisteAvsluttendeKalenderMåned = YearMonth.from(søknadsdato.minusMonths(2)),
        )

    private val arbeidssøkerRegisterGyldighetsperiode get() = prøvingsdato

    private val løsninger
        get() =
            mapOf(
                "Fødselsdato" to søknadsdato.minusYears(alder.toLong()),
                Søknadsdato to søknadsdato,
                ØnskerDagpengerFraDato to ønskerFraDato,
                // Inntekt
                InntektId to
                    mapOf(
                        "verdi" to inntektId,
                        // "gyldigTilOgMed" to 20.juni(2024),
                    ),
                "InntektSiste12Mnd" to InntektSiste12Mnd,
                "InntektSiste36Mnd" to InntektSiste36Mnd,
                // Reell arbeidssøker
                KanJobbeDeltid to true,
                KanJobbeHvorSomHelst to true,
                HelseTilAlleTyperJobb to true,
                VilligTilÅBytteYrke to true,
                // Arbeidssøkerregistrering
                RegistrertSomArbeidssøker to
                    mapOf(
                        "verdi" to true,
                        "gyldigFraOgMed" to arbeidssøkerRegisterGyldighetsperiode,
                        "gyldigTilOgMed" to arbeidssøkerRegisterGyldighetsperiode,
                    ),
                // Rettighetsype
                Ordinær to false,
                Permittert to true,
                Lønnsgaranti to false,
                PermittertFiskeforedling to false,
                // Verneplikt
                Verneplikt to false,
                TarUtdanningEllerOpplæring to false,
                Barnetillegg to
                    mapOf(
                        "verdi" to
                            listOf<Map<String, Any>>(
                                mapOf(
                                    "fødselsdato" to 1.januar(2000),
                                    "kvalifiserer" to true,
                                ),
                            ),
                    ),
                Inntekt to mapOf("verdi" to inntektV1),
                "Beregnet vanlig arbeidstid per uke før tap" to 40,
            )
}
