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
            period = 15.Minutt,
            action = {
                try {
                    vaktmesterRepository.loggOpplysningerSomSkalSlettes().also {
                        logger.info { "Skal slette ${it.size} fjernede opplysninger" }
                    }
                } catch (e: Exception) {
                    logger.error { "Sletting av fjernet opplysninger feilet: $e" }
                }
            },
        )
    }

    private val Int.Minutt get() = this * 1000L * 60L
}

private fun randomInitialDelay() = Random.nextInt(1..10).minutes.inWholeMilliseconds
