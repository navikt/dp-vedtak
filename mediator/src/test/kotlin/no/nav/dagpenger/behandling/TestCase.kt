package no.nav.dagpenger.behandling

import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import java.time.LocalDate

class TestCase(
    private val ident: String,
    private val rapid: TestRapid,
    private val søknadstidspunkt: LocalDate = LocalDate.of(2021, 5, 5),
    private val alder: Int = 30,
) {
    val inntektId = "01HQTE3GBWCSVYH6S436DYFREN"
    val søknadId = "e2e9e3e0-8e3e-4e3e-8e3e-0e3e8e3e9e3e"
    val behandlingId = "e3e9e3e0-8e3e-4e3e-8e3e-0e3e8e3e9e3e"

    fun sendSøknad() = rapid.sendTestMessage(søknadInnsendt())

    private fun søknadInnsendt() =
        JsonMessage.newMessage(
            "innsending_ferdigstilt",
            mapOf(
                "type" to "NySøknad",
                "fødselsnummer" to ident,
                "søknadsData" to
                        mapOf(
                            "søknad_uuid" to søknadId,
                        ),
            ),
        ).toJson()

    fun løsBehov(vararg behov: String) {
        val behovSomLøses = løsninger.filterKeys { it in behov }
        rapid.sendTestMessage(løstBehov(behovSomLøses))
    }

    private fun løstBehov(løsninger: Map<String, Any>) =
        JsonMessage.newMessage(
            "behov",
            mapOf(
                "ident" to ident,
                "behandlingId" to behandlingId,
                "@final" to true,
                "@løsning" to løsninger,
            ),
        ).toJson()

    private val løsninger =
        mapOf(
            "Fødselsdato" to søknadstidspunkt.minusYears(alder.toLong()),
            "Søknadstidspunkt" to søknadstidspunkt,
            "ØnskerDagpengerFraDato" to søknadstidspunkt,
            "InntektId" to inntektId,
            "InntektSiste12Mnd" to 1234,
            "InntektSiste36Mnd" to 1234,
        )
}
