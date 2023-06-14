package no.nav.dagpenger.vedtak.mediator

import mu.KotlinLogging
import no.nav.dagpenger.aktivitetslogg.Aktivitetslogg
import no.nav.dagpenger.vedtak.mediator.persistens.PersonRepository
import no.nav.dagpenger.vedtak.modell.Person
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
        finalize(hendelse).also {
            delegatedObserver.finalize()
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
            else -> person ?: throw RuntimeException("har ikke informasjon om person")
        }
    }

    private fun finalize(hendelse: Hendelse) {
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

    private val vedtakDelegate = mutableListOf<Pair<String, VedtakObserver.RammevedtakFattet>>()
    private val løpendeVedtakDelegate = mutableListOf<Pair<String, VedtakObserver.LøpendeVedtakFattet>>()

    override fun rammevedtakFattet(ident: String, rammevedtakFattet: VedtakObserver.RammevedtakFattet) {
        vedtakDelegate.add(Pair(ident, rammevedtakFattet))
    }

    override fun løpendeVedtakFattet(ident: String, løpendeVedtakFattet: VedtakObserver.LøpendeVedtakFattet) {
        løpendeVedtakDelegate.add(Pair(ident, løpendeVedtakFattet))
    }

    fun finalize() {
        vedtakDelegate.forEach { (ident, vedtak) ->
            observers.forEach {
                it.rammevedtakFattet(ident, vedtak)
            }
        }
        løpendeVedtakDelegate.forEach { (ident, vedtak) ->
            observers.forEach {
                it.løpendeVedtakFattet(ident, vedtak)
            }
        }
    }
}
