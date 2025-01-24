package no.nav.dagpenger.regel

import com.spun.util.persistence.Loader
import org.approvaltests.Approvals
import org.approvaltests.core.Options
import org.approvaltests.namer.NamerWrapper
import org.junit.jupiter.api.Test
import java.nio.file.Paths

class OpplysningDokumentasjon {
    @Test
    fun `henter ut hvilken informasjon som må innhentes`() {
        val regler = RegelverkDagpenger.regelsett

        val behov = regler.flatMap { it.behov }

        val markdown =
            """
            ># Dokumentasjon på behov for opplysninger
            >
            >Dette er opplysninger som blir innhentet som en del av dagpengebehandlingen. De publiseres som behov på rapiden.
            >
            >|Behov|Beskrivelse|Logisk datatype|Datatype|
            >|---|---|---|---|
            ${
                behov.sortedBy { it.behovId }.joinToString("\n") {
                    ">|${it.behovId} | ${it.navn} | ${it.datatype}|${it.datatype.klasse.simpleName}|"
                }
            }
            """.trimMargin(">")

        skriv(markdown)
    }

    private companion object {
        val path = "${Paths.get("").toAbsolutePath().toString().substringBeforeLast("/")}/docs/"
        val options = Options().forFile().withExtension(".md")
    }

    private fun skriv(behov: String) {
        Approvals.namerCreater = Loader { NamerWrapper({ "behov" }, { path }) }
        Approvals.verify(behov, options)
    }
}
