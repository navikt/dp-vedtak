package no.nav.dagpenger.behandling.mediator.api.auth.validering

import io.ktor.server.auth.jwt.JWTAuthenticationProvider
import io.ktor.server.auth.jwt.JWTCredential
import io.ktor.server.auth.jwt.JWTPrincipal
import no.nav.dagpenger.behandling.konfigurasjon.Configuration

internal fun JWTAuthenticationProvider.Config.autoriserADGrupper() {
    val saksbehandlerGruppe = Configuration.properties[Configuration.Grupper.saksbehandler]

    validate { jwtClaims ->
        jwtClaims.måInneholde(ADGruppe = saksbehandlerGruppe)
        JWTPrincipal(jwtClaims.payload)
    }
}

private fun JWTCredential.måInneholde(ADGruppe: String) =
    require(
        this.payload.claims["groups"]
            ?.asList(String::class.java)
            ?.contains(ADGruppe) ?: false,
    ) { "Mangler tilgang" }
