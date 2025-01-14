package no.nav.dagpenger.behandling.mediator.jobber

import mu.KotlinLogging
import no.nav.dagpenger.behandling.mediator.repository.VaktmesterPostgresRepo
import kotlin.concurrent.fixedRateTimer
import kotlin.random.Random
import kotlin.random.nextInt
import kotlin.time.Duration.Companion.minutes

internal object SlettFjernetOpplysninger {
    private val logger = KotlinLogging.logger {}

    fun slettOpplysninger(vaktmesterRepository: VaktmesterPostgresRepo) {
        fixedRateTimer(
            name = "Slett fjernet opplysninger",
            daemon = true,
            initialDelay = randomInitialDelay(),
            period = 15.minutes.inWholeMilliseconds,
            action = {
                try {
                    if (System.getenv("NAIS_CLUSTER_NAME") == "dev-gcp") {
                        vaktmesterRepository.slettOpplysninger(antall = 1).also {
                            logger.info { "Har slettet ${it.size} fjernede opplysninger" }
                        }
                    }
                } catch (e: Exception) {
                    logger.error { "Sletting av fjernet opplysninger feilet: $e" }
                }
            },
        )
    }
}

private fun randomInitialDelay() = Random.nextInt(1..10).minutes.inWholeMilliseconds
