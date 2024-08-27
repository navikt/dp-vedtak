package no.nav.dagpenger.behandling.konfigurasjon

import com.natpryce.konfig.Key
import com.natpryce.konfig.stringType
import io.getunleash.DefaultUnleash
import io.getunleash.FakeUnleash
import io.getunleash.Unleash
import io.getunleash.util.UnleashConfig
import mu.KotlinLogging
import no.nav.dagpenger.behandling.konfigurasjon.Configuration.properties
import java.net.InetAddress

private val logger = KotlinLogging.logger { }
internal val unleash: Unleash =
    if (properties.getOrNull(Key("UNLEASH_SERVER_API_URL", stringType)) == null) {
        FakeUnleash()
    } else {
        DefaultUnleash(
            UnleashConfig
                .builder()
                .appName(properties[Key("NAIS_APP_NAME", stringType)])
                .instanceId(runCatching { InetAddress.getLocalHost().hostName }.getOrElse { "ukjent" })
                .unleashAPI(properties[Key("UNLEASH_SERVER_API_URL", stringType)] + "/api/")
                .apiKey(properties[Key("UNLEASH_SERVER_API_TOKEN", stringType)])
                .environment(
                    when (System.getenv("NAIS_CLUSTER_NAME").orEmpty()) {
                        "prod-gcp" -> "production"
                        else -> "development"
                    },
                ).build(),
        )
    }

fun skruPåFeature(feature: String) {
    require(unleash is FakeUnleash)
    unleash.enable(feature)
}

fun skruAvFeatures() {
    require(unleash is FakeUnleash)
    unleash.resetAll()
}

val støtterInnvilgelse
    get() =
        unleash.isEnabled("dp-behandling.innvilgelse").also {
            if (it) {
                logger.info("Feature dp-behandling.innvilgelse er aktivert")
            } else {
                logger.info("Feature dp-behandling.innvilgelse er deaktivert")
            }
        }
