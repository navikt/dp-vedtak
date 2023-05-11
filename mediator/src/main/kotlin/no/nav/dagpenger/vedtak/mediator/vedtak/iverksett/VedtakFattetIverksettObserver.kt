package no.nav.dagpenger.vedtak.mediator.vedtak.iverksett

import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import no.nav.dagpenger.vedtak.iverksetting.mediator.behovløsere.IverksettClient
import no.nav.dagpenger.vedtak.modell.PersonObserver
import no.nav.dagpenger.vedtak.modell.vedtak.VedtakObserver
import no.nav.dagpenger.vedtak.modell.vedtak.VedtakObserver.VedtakFattet.Utfall.Innvilget

internal class VedtakFattetIverksettObserver(private val iverksettClient: IverksettClient) : PersonObserver {

    private val logger = KotlinLogging.logger { }

    override fun vedtaktFattet(ident: String, vedtakFattet: VedtakObserver.VedtakFattet) {
        val iverksettDagpengerdDto = vedtakFattet.tilIverksettDto(ident)

        if (vedtakFattet.utfall == Innvilget) {
            runBlocking {
                iverksettClient.iverksett(iverksettDagpengerdDto)
            }
        } else {
            logger.warn { "Kan ikke iverksette vedtak med id ${vedtakFattet.vedtakId} den har annet utfall enn Innvilget" }
        }
    }
}
