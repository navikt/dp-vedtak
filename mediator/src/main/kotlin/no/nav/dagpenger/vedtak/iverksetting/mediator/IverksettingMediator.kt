package no.nav.dagpenger.vedtak.iverksetting.mediator

import mu.KotlinLogging
import no.nav.dagpenger.aktivitetslogg.Aktivitetslogg
import no.nav.dagpenger.vedtak.iverksetting.Iverksetting
import no.nav.dagpenger.vedtak.iverksetting.hendelser.IverksattHendelse
import no.nav.dagpenger.vedtak.iverksetting.hendelser.VedtakFattetHendelse
import no.nav.dagpenger.vedtak.iverksetting.mediator.persistens.IverksettingRepository
import no.nav.dagpenger.vedtak.mediator.BehovMediator
import no.nav.dagpenger.vedtak.modell.hendelser.Hendelse
import no.nav.helse.rapids_rivers.withMDC

internal class IverksettingMediator(
    private val iverksettingRepository: IverksettingRepository,
    private val behovMediator: BehovMediator,
) {

    private companion object {
        val logger = KotlinLogging.logger { }
        val sikkerLogger = KotlinLogging.logger("tjenestekall.IverksettingMediator")
    }

    fun håndter(vedtakFattetHendelse: VedtakFattetHendelse) {
        håndter(vedtakFattetHendelse) { iverksetting ->
            iverksetting.håndter(vedtakFattetHendelse)
        }
    }

    fun håndter(iverksattHendelse: IverksattHendelse) {
        håndter(iverksattHendelse) { iverksetting ->
            iverksetting.håndter(iverksattHendelse)
        }
    }

    private fun håndter(hendelse: Hendelse, håndter: (Iverksetting) -> Unit) = try {
        val iverksetting = hentEllerOpprettIverksett(hendelse)
        håndter(iverksetting)
        iverksettingRepository.lagre(iverksetting)
        finalize(hendelse)
    } catch (err: Aktivitetslogg.AktivitetException) {
        logger.error("alvorlig feil i aktivitetslogg (se sikkerlogg for detaljer)")
        withMDC(err.kontekst()) {
            sikkerLogger.error("alvorlig feil i aktivitetslogg: ${err.message}", err)
        }
        throw err
    } catch (oie: OpprettIverksettingException) {
        sikkerLogger.error { oie }
    } catch (e: Exception) {
        errorHandler(e, e.message ?: "Ukjent feil")
        throw e
    }

    private fun hentEllerOpprettIverksett(hendelse: Hendelse): Iverksetting {
        return when (hendelse) {
            is VedtakFattetHendelse -> iverksettingRepository.hent(hendelse.iverksettingsVedtak.vedtakId)
                ?: Iverksetting(hendelse.iverksettingsVedtak.vedtakId, hendelse.ident())

            is IverksattHendelse -> iverksettingRepository.hent(hendelse.vedtakId)
                ?: throw OpprettIverksettingException("Kan ikke knytte iverksatthendelse til en Iverksetting")

            else -> {
                TODO("Støtter bare VedtakFattetHendelse pt")
            }
        }
    }

    class OpprettIverksettingException(message: String) : RuntimeException(message)

    private fun finalize(hendelse: Hendelse) {
        // if (!hendelse.hasMessages()) return
        // if (hendelse.hasErrors()) return sikkerLogger.info("aktivitetslogg inneholder errors: ${hendelse.toLogString()}")
        sikkerLogger.info("aktivitetslogg inneholder meldinger: ${hendelse.toLogString()}")
        behovMediator.håndter(hendelse)
    }

    private fun errorHandler(err: Exception, message: String, context: Map<String, String> = emptyMap()) {
        logger.error("alvorlig feil: ${err.message} (se sikkerlogg for melding)", err)
        withMDC(context) { sikkerLogger.error("alvorlig feil: ${err.message}\n\t$message", err) }
    }
}
