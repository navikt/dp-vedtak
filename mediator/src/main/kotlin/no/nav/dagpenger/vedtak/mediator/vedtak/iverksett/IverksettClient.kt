package no.nav.dagpenger.vedtak.mediator.vedtak.iverksett

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.jackson.jackson
import no.nav.dagpenger.vedtak.mediator.vedtak.iverksett.models.IverksettDagpengerdDto

internal class IverksettClient(engine: HttpClientEngine = CIO.create()) {

    private val httpClient = HttpClient(engine) {
        expectSuccess = true
        install(ContentNegotiation) {
            jackson {
                registerModule(JavaTimeModule())
            }
        }
    }

    suspend fun iverksett(iverksettDagpengerdDto: IverksettDagpengerdDto) {
        // api/iverksetting

        // kast feil eller ok
    }
}
