package no.nav.dagpenger.features

import io.cucumber.java8.HookBody
import io.cucumber.java8.No
import no.nav.dagpenger.opplysning.Opplysninger
import no.nav.dagpenger.opplysning.Regelkjøring
import org.approvaltests.Approvals
import org.approvaltests.core.Options
import org.approvaltests.namer.NamerWrapper
import java.nio.file.Paths

interface RegelTest : No {
    val opplysninger: Opplysninger
    val regelkjøring: Regelkjøring

    private companion object {
        val path = "${
            Paths.get("").toAbsolutePath().toString().substringBeforeLast("/")
        }/docs/"
        val options =
            Options()
                .forFile()
                .withExtension(".md")
    }

    fun skriv(
        tittel: String,
        dokumentasjon: String,
    ) {
        Approvals.namerCreater = com.spun.util.persistence.Loader { NamerWrapper({ "regler/$tittel" }, { path }) }
        Approvals
            .verify(
                dokumentasjon,
                options,
            )
    }

    override fun AfterStep(body: HookBody?) {
        if (body != null) {
            val dokumentasjon = body.toString()
        }
    }
}
