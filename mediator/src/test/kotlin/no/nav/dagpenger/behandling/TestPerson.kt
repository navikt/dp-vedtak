package no.nav.dagpenger.behandling

import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers.test_support.TestRapid
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
import no.nav.dagpenger.regel.Behov.TarUtdanningEllerOpplæring
import no.nav.dagpenger.regel.Behov.Verneplikt
import no.nav.dagpenger.regel.Behov.VilligTilÅBytteYrke
import org.intellij.lang.annotations.Language
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth

class TestPerson(
    private val ident: String,
    private val rapid: TestRapid,
    internal val søknadstidspunkt: LocalDate = 5.mai(2021),
    val alder: Int = 30,
    private val innsendt: LocalDateTime = LocalDateTime.now(),
    val InntektSiste12Mnd: Int = 1234,
    val InntektSiste36Mnd: Int = 1234,
    internal var ønskerFraDato: LocalDate = søknadstidspunkt,
) {
    val inntektId = "01HQTE3GBWCSVYH6S436DYFREN"
    internal val søknadId = "4afce924-6cb4-4ab4-a92b-fe91e24f31bf"
    private val behandlingId by lazy { rapid.inspektør.field(1, "behandlingId").asText() }

    fun sendSøknad() = rapid.sendTestMessage(søknadInnsendt())

    private fun søknadInnsendt() =
        JsonMessage
            .newMessage(
                "innsending_ferdigstilt",
                mapOf(
                    "@opprettet" to innsendt,
                    "type" to "NySøknad",
                    "fødselsnummer" to ident,
                    "fagsakId" to 123,
                    "bruk-dp-behandling" to true,
                    "søknadsData" to
                        mapOf(
                            "søknad_uuid" to søknadId,
                        ),
                ),
            ).toJson()

    fun løsBehov(vararg behov: String) {
        val behovSomLøses = løsninger.filterKeys { it in behov }
        require(behovSomLøses.size == behov.size) { "Fant ikke løsning for alle behov: $behov" }
        rapid.sendTestMessage(løstBehov(behovSomLøses))
    }

    fun løsBehov(
        behov: String,
        løsning: Any,
        data: Map<String, Any> = emptyMap(),
    ) {
        rapid.sendTestMessage(løstBehov(mapOf(behov to løsning), false, data))
    }

    private fun løstBehov(
        løsninger: Map<String, Any>,
        opplysningsbehov: Boolean = true,
        data: Map<String, Any> = emptyMap(),
    ) = JsonMessage
        .newMessage(
            "behov",
            mapOf(
                "ident" to ident,
                "behandlingId" to behandlingId,
                "søknadId" to søknadId,
                "@opplysningsbehov" to opplysningsbehov,
                "@behov" to løsninger.keys.toList(),
                "@final" to true,
                "@løsning" to løsninger,
            ) + data,
        ).toJson()

    fun markerAvklaringIkkeRelevant(
        avklaringId: String,
        kode: String,
    ) {
        rapid.sendTestMessage(avklaringIkkeRelevant(avklaringId, kode))
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
        )
    }

    @Language("JSON")
    private val løsningPåInntekt =
        """
        {
          "inntektsId": "01J677GHJRC2H08Q55DASFD0XX",
          "inntektsListe": [
            {
              "årMåned": "${YearMonth.from(søknadstidspunkt.minusMonths(2))}",
              "klassifiserteInntekter": [
                {
                  "beløp": 41600.0,
                  "inntektKlasse": "ARBEIDSINNTEKT"
                }
              ],
              "harAvvik": false
            },
            {
              "årMåned": "${YearMonth.from(søknadstidspunkt.minusMonths(3))}",
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

    private val lollerhino =
        no.nav.dagpenger.inntekt.v1.Inntekt(
            inntektsId = "01J677GHJRC2H08Q55DASFD0XX",
            inntektsListe = emptyList(),
            sisteAvsluttendeKalenderMåned = YearMonth.from(søknadstidspunkt.minusMonths(2)),
        )

    private val løsninger get() =
        mapOf(
            "Fødselsdato" to søknadstidspunkt.minusYears(alder.toLong()),
            "Søknadstidspunkt" to søknadstidspunkt,
            "ØnskerDagpengerFraDato" to ønskerFraDato,
            // Inntekt
            InntektId to mapOf("verdi" to inntektId),
            "InntektSiste12Mnd" to InntektSiste12Mnd,
            "InntektSiste36Mnd" to InntektSiste36Mnd,
            // Reell arbeidssøker
            KanJobbeDeltid to true,
            KanJobbeHvorSomHelst to true,
            HelseTilAlleTyperJobb to true,
            VilligTilÅBytteYrke to true,
            // Arbeidssøkerregistrering
            RegistrertSomArbeidssøker to true,
            // Rettighetsype
            Ordinær to false,
            Permittert to true,
            Lønnsgaranti to false,
            PermittertFiskeforedling to false,
            // Verneplikt
            Verneplikt to false,
            TarUtdanningEllerOpplæring to false,
            Inntekt to mapOf("verdi" to lollerhino),
        )
}
