package no.nav.dagpenger.behandling.mediator.mottak

import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers.River
import com.github.navikt.tbd_libs.rapids_and_rivers.asLocalDate
import com.github.navikt.tbd_libs.rapids_and_rivers.asLocalDateTime
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageContext
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageMetadata
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import io.micrometer.core.instrument.MeterRegistry
import io.opentelemetry.api.trace.Span
import io.opentelemetry.instrumentation.annotations.WithSpan
import mu.KotlinLogging
import mu.withLoggingContext
import no.nav.dagpenger.behandling.mediator.IMessageMediator
import no.nav.dagpenger.behandling.mediator.MessageMediator
import no.nav.dagpenger.behandling.mediator.asUUID
import no.nav.dagpenger.behandling.mediator.melding.HendelseMessage
import no.nav.dagpenger.behandling.modell.hendelser.AktivitetType
import no.nav.dagpenger.behandling.modell.hendelser.Dag
import no.nav.dagpenger.behandling.modell.hendelser.MeldekortAktivitet
import no.nav.dagpenger.behandling.modell.hendelser.MeldekortHendelse
import no.nav.dagpenger.behandling.modell.hendelser.MeldekortKilde
import kotlin.time.Duration

internal class MeldekortMottak(
    rapidsConnection: RapidsConnection,
    private val messageMediator: MessageMediator,
) : River.PacketListener {
    init {
        River(rapidsConnection)
            .apply {
                precondition { it.requireValue("@event_name", "rapporteringsperiode_innsendt_hendelse") }
                validate { it.requireKey("ident") }
                validate { it.requireKey("rapporteringsId") }
                validate { it.requireKey("fom") }
                validate { it.requireKey("tom") }
                validate { it.requireKey("kilde") }
                validate { it.requireKey("dager") }
            }.register(this)
    }

    @WithSpan
    override fun onPacket(
        packet: JsonMessage,
        context: MessageContext,
        metadata: MessageMetadata,
        meterRegistry: MeterRegistry,
    ) {
        val meldekortId = packet["rapporteringsId"].asUUID()
        Span.current().apply {
            setAttribute("app.river", name())
            setAttribute("app.meldekortId", meldekortId.toString())
        }

        withLoggingContext(
            "meldekortId" to meldekortId.toString(),
        ) {
            val message = MeldekortMessage(packet)
            message.behandle(messageMediator, context)
        }
    }

    private companion object {
        private val logger = KotlinLogging.logger {}
    }
}

internal class MeldekortMessage(
    private val packet: JsonMessage,
) : HendelseMessage(packet) {
    override val ident get() = packet["ident"].asText()

    override fun behandle(
        mediator: IMessageMediator,
        context: MessageContext,
    ) {
        withLoggingContext(hendelse.kontekstMap()) {
            logger.info { "Behandler meldekort: ${hendelse.meldekortId}" }
            try {
                mediator.behandle(hendelse, this, context)
            } catch (e: Exception) {
                logger.error(e) {
                    // TODO Finn en bedre feilmelding
                    "Kan ikke å behandle ${hendelse.javaClass.simpleName} av en eller annen grunn"
                }
            }
        }
    }

    private val hendelse
        get() =
            MeldekortHendelse(
                meldingsreferanseId = packet["@id"].asUUID(),
                ident = packet["ident"].asText(),
                meldekortId = packet["rapporteringsId"].asUUID(),
                fom = packet["fom"].asLocalDate(),
                tom = packet["tom"].asLocalDate(),
                kilde =
                    MeldekortKilde(
                        rolle = packet["kilde"]["rolle"].asText(),
                        ident = packet["kilde"]["ident"].asText(),
                    ),
                opprettet = packet["@opprettet"].asLocalDateTime(),
                dager =
                    packet["dager"].map { dag ->
                        Dag(
                            dato = dag["dato"].asLocalDate(),
                            aktiviteter =
                                dag["aktiviteter"].map {
                                    MeldekortAktivitet(
                                        type = AktivitetType.valueOf(it["type"].asText()),
                                        tid = Duration.parseIsoString(it["tid"].asText()),
                                    )
                                },
                        )
                    },
            )

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}
