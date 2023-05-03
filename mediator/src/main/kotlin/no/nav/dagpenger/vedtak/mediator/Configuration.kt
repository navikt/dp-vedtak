package no.nav.dagpenger.vedtak.mediator

import com.natpryce.konfig.ConfigurationMap
import com.natpryce.konfig.ConfigurationProperties
import com.natpryce.konfig.EnvironmentVariables
import com.natpryce.konfig.Key
import com.natpryce.konfig.overriding
import com.natpryce.konfig.stringType
import kotlinx.coroutines.runBlocking
import no.nav.dagpenger.oauth2.CachedOauth2Client
import no.nav.dagpenger.oauth2.OAuth2Config

internal object Configuration {

    private val defaultProperties = ConfigurationMap(
        mapOf(
            "RAPID_APP_NAME" to "dp-vedtak",
            "KAFKA_CONSUMER_GROUP_ID" to "dp-vedtak-v1",
            "KAFKA_RAPID_TOPIC" to "teamdagpenger.rapid.v1",
            "KAFKA_RESET_POLICY" to "latest",
        ),
    )
    private val properties = ConfigurationProperties.systemProperties() overriding EnvironmentVariables() overriding defaultProperties

    val config: Map<String, String> = properties.list().reversed().fold(emptyMap()) { map, pair ->
        map + pair.second
    }

    private val azureAdClient: CachedOauth2Client by lazy {
        val azureAdConfig = OAuth2Config.AzureAd(config)
        CachedOauth2Client(
            tokenEndpointUrl = azureAdConfig.tokenEndpointUrl,
            authType = azureAdConfig.clientSecret(),
        )
    }

    internal val iverksettApiUrl: String by lazy { properties[Key("DP_IVERKSETT_URL", stringType)] }

    internal val iverksettClientTokenSupplier by lazy {
        {
            runBlocking {
                azureAdClient.clientCredentials(
                    properties[
                        Key(
                            "DP_IVERKSETT_SCOPE",
                            stringType,
                        ),
                    ],
                ).accessToken
            }
        }
    }
}
