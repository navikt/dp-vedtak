package no.nav.dagpenger.features

import com.spun.util.persistence.Loader
import io.cucumber.messages.types.Envelope
import io.cucumber.plugin.ConcurrentEventListener
import io.cucumber.plugin.event.EventPublisher
import io.cucumber.plugin.event.TestRunFinished
import org.approvaltests.Approvals
import org.approvaltests.core.Options
import org.approvaltests.namer.NamerWrapper
import java.nio.file.Paths

class RegeltreDokumentasjonPlugin : ConcurrentEventListener {
    private lateinit var gherkinSource: String
    private lateinit var regeltreDiagram: String
    private lateinit var navn: String

    override fun setEventPublisher(publisher: EventPublisher) {
        publisher.registerHandlerFor(TestRunFinished::class.java) { _ ->
            if (this::regeltreDiagram.isInitialized) {
                val markdown =
                    """
                    |# $navn
                    | 
                    | ## Regletre
                    |
                    |```mermaid
                    |$regeltreDiagram
                    |```
                    |
                    | ## Akseptansetester
                    |
                    |```gherkin
                    |$gherkinSource
                    |```
                    """.trimMargin()
                skriv(
                    navn,
                    markdown,
                )
            }
        }
        publisher.registerHandlerFor(Envelope::class.java) { event ->

            if (event.gherkinDocument.isPresent) {
                navn = event.gherkinDocument.get().feature.get().name
            }
            if (event.source.isPresent) {
                gherkinSource = event.source.get().data
            }

            if (event.attachment.isPresent) {
                val attachment = event.attachment.get()
                regeltreDiagram = attachment.body
            }
        }
    }

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
        Approvals.namerCreater = Loader { NamerWrapper({ "regler/$tittel" }, { path }) }
        Approvals
            .verify(
                dokumentasjon,
                options,
            )
    }
}
