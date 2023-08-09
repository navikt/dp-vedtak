package no.nav.dagpenger.vedtak.mediator.api

import io.ktor.server.application.Application
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import no.nav.dagpenger.vedtak.mediator.Configuration
import no.nav.security.mock.oauth2.MockOAuth2Server

object TestApplication {
    private const val AZUREAD_ISSUER_ID = "azureAd"
    private const val CLIENT_ID = "dp-soknad"

    private val mockOAuth2Server: MockOAuth2Server by lazy {
        MockOAuth2Server().also { server ->
            server.start()
        }
    }

    internal val testAzureAdToken: String by lazy {
        mockOAuth2Server.issueToken(
            issuerId = AZUREAD_ISSUER_ID,
            audience = CLIENT_ID,
            claims = mapOf(
                "NAVident" to "123",
                "groups" to listOf(
                    Configuration.properties[Configuration.Grupper.saksbehandler],
                ),
            ),
        ).serialize()
    }

    internal fun withMockAuthServerAndTestApplication(
        moduleFunction: Application.() -> Unit,
        test: suspend ApplicationTestBuilder.() -> Unit,
    ) {
        System.setProperty("azure-app.client-id", CLIENT_ID)
        System.setProperty("azure-app.well-known-url", "${mockOAuth2Server.wellKnownUrl(AZUREAD_ISSUER_ID)}")

        return testApplication {
            application(moduleFunction)
            test()
        }
    }
}
