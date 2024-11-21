package no.nav.dagpenger.behandling.mediator.jobber

import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageContext
import kotliquery.queryOf
import kotliquery.sessionOf
import mu.KotlinLogging
import no.nav.dagpenger.behandling.db.PostgresDataSourceBuilder.dataSource
import no.nav.dagpenger.behandling.modell.Behandling.TilstandType.ForslagTilVedtak
import no.nav.dagpenger.regel.KravPåDagpenger.kravPåDagpenger
import kotlin.concurrent.fixedRateTimer
import kotlin.random.Random
import kotlin.random.nextInt
import kotlin.time.Duration.Companion.minutes

internal class AvbrytInnvilgelse(
    val rapid: MessageContext,
) {
    private val logger = KotlinLogging.logger {}

    fun start(antallDager: Int) {
        if (antallDager < 1) {
            logger.error { "Antall dager er mindre enn 1, starter ikke jobb for å avbryte" }
            return
        }

        fixedRateTimer(
            name = "Abryter behandlinger som står i forslag til vedtak om innvilgelse",
            daemon = true,
            initialDelay = randomInitialDelay(),
            period = 15.minutes.inWholeMilliseconds,
            action = {
                try {
                    avbrytBehandlinger(antallDager)
                } catch (e: Exception) {
                    logger.error(e) { "Sletting av behandlinger feilet" }
                }
            },
        )
    }

    internal fun avbrytBehandlinger(antallDager: Int) {
        val behandlinger = finnBehandlinger(antallDager)
        logger.info { "Fant ${behandlinger.size} behandlinger som skal avbrytes" }
        behandlinger.forEach {
            logger.info { "Avbryter behandling ${it.behandlingId}" }
            rapid.publish(it.ident, it.tilJson())
        }
    }

    private fun BehandlingTilAvbryt.tilJson() =
        JsonMessage
            .newMessage(
                "avbryt_behandling",
                mapOf(
                    "ident" to ident,
                    "behandlingId" to behandlingId,
                    "årsak" to "Avbryter behandling som har stått i forslag til vedtak om innvilgelse for lenge",
                ),
            ).toJson()

    private fun finnBehandlinger(antallDager: Int) =
        sessionOf(dataSource).use { session ->
            session.transaction { tx ->
                tx.run(queryOf("SELECT pg_try_advisory_lock(555544443333)").asExecute)
                tx.run(
                    queryOf(
                        //language=PostgreSQL
                        """
                        SELECT p.ident,
                               b.behandling_id
                        FROM behandling b
                                 JOIN person_behandling pb
                                      ON pb.behandling_id = b.behandling_id
                                 JOIN person p
                                      ON p.ident = pb.ident
                                 JOIN behandling_opplysninger bo
                                      ON bo.behandling_id = b.behandling_id
                                 JOIN opplysningstabell ot
                                      ON ot.opplysninger_id = bo.opplysninger_id
                        WHERE b.tilstand = '${ForslagTilVedtak.name}'
                          AND ot.type_id = '${kravPåDagpenger.id}'
                          AND ot.verdi_boolsk = TRUE
                          AND b.sist_endret_tilstand < NOW() - INTERVAL '$antallDager DAY'
                        """.trimIndent(),
                    ).map { row ->
                        BehandlingTilAvbryt(
                            ident = row.string("ident"),
                            behandlingId = row.string("behandling_id"),
                        )
                    }.asList,
                )
            }
        }

    private data class BehandlingTilAvbryt(
        val ident: String,
        val behandlingId: String,
    )
}

private fun randomInitialDelay() = Random.nextInt(1..10).minutes.inWholeMilliseconds
