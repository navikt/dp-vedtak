package no.nav.dagpenger.vedtak.iverksetting.mediator

import mu.KotlinLogging
import no.nav.dagpenger.vedtak.iverksetting.Iverksetting
import no.nav.dagpenger.vedtak.iverksetting.hendelser.VedtakFattetHendelse
import no.nav.dagpenger.vedtak.iverksetting.mediator.persistens.IverksettingRepository
import no.nav.dagpenger.vedtak.modell.Aktivitetslogg
import no.nav.helse.rapids_rivers.withMDC

// @todo
// 1. Hente eller opprette iverksetting - IverksettingRepository
// 2. Håndtere hendelse
// 3. Lagre iverksetting
// 4. Behovmediator - fra dp-soknad
// 5. Lage behovløser for iversksetting mot iver

internal class IverksettingMediator(private val iverksettingRepository: IverksettingRepository) {

    private companion object {
        val logger = KotlinLogging.logger { }
        val sikkerLogger = KotlinLogging.logger("tjenestekall.IverksettingMediator")
    }

    fun håndter(vedtakFattetHendelse: VedtakFattetHendelse) {
        håndter(vedtakFattetHendelse) { iverksetting ->
            iverksetting.håndter(vedtakFattetHendelse)
        }
    }

    private fun håndter(hendelse: VedtakFattetHendelse, håndter: (Iverksetting) -> Unit) = try {
        val iverksetting = Iverksetting(hendelse.iverksettingsVedtak.vedtakId)
        håndter(iverksetting)
        iverksettingRepository.lagre(iverksetting)
    } catch (err: Aktivitetslogg.AktivitetException) {
        logger.error("alvorlig feil i aktivitetslogg (se sikkerlogg for detaljer)")
        withMDC(err.kontekst()) {
            sikkerLogger.error("alvorlig feil i aktivitetslogg: ${err.message}", err)
        }
        throw err
    } catch (e: Exception) {
        errorHandler(e, e.message ?: "Ukjent feil")
        throw e
    }

    private fun errorHandler(err: Exception, message: String, context: Map<String, String> = emptyMap()) {
        logger.error("alvorlig feil: ${err.message} (se sikkerlogg for melding)", err)
        withMDC(context) { sikkerLogger.error("alvorlig feil: ${err.message}\n\t$message", err) }
    }
}
