package no.nav.dagpenger.vedtak.mediator

import mu.KotlinLogging
import no.nav.dagpenger.aktivitetslogg.Aktivitetslogg
import no.nav.dagpenger.vedtak.mediator.persistens.PersonRepository
import no.nav.dagpenger.vedtak.modell.Person
import no.nav.dagpenger.vedtak.modell.PersonIdentifikator
import no.nav.dagpenger.vedtak.modell.PersonIdentifikator.Companion.tilPersonIdentfikator
import no.nav.dagpenger.vedtak.modell.PersonObserver
import no.nav.dagpenger.vedtak.modell.hendelser.Hendelse
import no.nav.dagpenger.vedtak.modell.hendelser.Rapporteringshendelse
import no.nav.dagpenger.vedtak.modell.hendelser.SøknadBehandletHendelse
import no.nav.dagpenger.vedtak.modell.vedtak.VedtakObserver
import no.nav.helse.rapids_rivers.withMDC

internal class PersonMediator(
    private val personRepository: PersonRepository,
    private val personObservers: List<PersonObserver> = emptyList(),
) {
    private companion object {
        val logger = KotlinLogging.logger { }
        val sikkerLogger = KotlinLogging.logger("tjenestekall")
    }

    fun håndter(søknadBehandletHendelse: SøknadBehandletHendelse) {
        behandle(søknadBehandletHendelse) { person ->
            person.håndter(søknadBehandletHendelse)
        }
    }

    fun håndter(rapporteringshendelse: Rapporteringshendelse) {
        behandle(rapporteringshendelse) { person ->
            person.håndter(rapporteringshendelse)
        }
    }

    private fun behandle(hendelse: Hendelse, håndter: (Person) -> Unit) = try {
        val person = hentEllerOpprettPerson(hendelse)
        val delegatedObserver = DelegatedObserver(personObservers)
        person.addObserver(delegatedObserver)
        håndter(person)
        lagre(person)
        ferdigstill(hendelse).also {
            delegatedObserver.ferdigstill()
        }
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

    private fun lagre(person: Person) {
        personRepository.lagre(person)
    }

    private fun hentEllerOpprettPerson(hendelse: Hendelse): Person {
        val person = personRepository.hent(hendelse.ident().tilPersonIdentfikator())
        return when (hendelse) {
            is SøknadBehandletHendelse -> person ?: Person(hendelse.ident().tilPersonIdentfikator())
            else -> person ?: Person(PersonIdentifikator("12345123451"))
                .also { logger.error { "Oppretter default person 👨🏽" } } // TODO: Fjern når vi har database
        }
    }

    private fun ferdigstill(hendelse: Hendelse) {
        // if (!hendelse.hasMessages()) return
        // if (hendelse.hasErrors()) return sikkerLogger.info("aktivitetslogg inneholder errors: ${hendelse.toLogString()}")
        sikkerLogger.info("aktivitetslogg inneholder meldinger: ${hendelse.toLogString()}")
    }

    private fun errorHandler(err: Exception, message: String, context: Map<String, String> = emptyMap()) {
        logger.error("alvorlig feil: ${err.message} (se sikkerlogg for melding)", err)
        withMDC(context) { sikkerLogger.error("alvorlig feil: ${err.message}\n\t$message", err) }
    }
}

private class DelegatedObserver(private val observers: List<PersonObserver>) : PersonObserver {

    private val vedtakDelegate = mutableListOf<Pair<String, VedtakObserver.VedtakFattet>>()
    private val løpendeVedtakDelegate = mutableListOf<Pair<String, VedtakObserver.UtbetalingsvedtakFattet>>()

    override fun vedtakFattet(ident: String, vedtakFattet: VedtakObserver.VedtakFattet) {
        vedtakDelegate.add(Pair(ident, vedtakFattet))
    }

    override fun utbetalingsvedtakFattet(ident: String, utbetalingsvedtakFattet: VedtakObserver.UtbetalingsvedtakFattet) {
        løpendeVedtakDelegate.add(Pair(ident, utbetalingsvedtakFattet))
    }

    fun ferdigstill() {
        vedtakDelegate.forEach { (ident, vedtak) ->
            observers.forEach {
                it.vedtakFattet(ident, vedtak)
            }
        }
        løpendeVedtakDelegate.forEach { (ident, vedtak) ->
            observers.forEach {
                it.utbetalingsvedtakFattet(ident, vedtak)
            }
        }
    }
}
