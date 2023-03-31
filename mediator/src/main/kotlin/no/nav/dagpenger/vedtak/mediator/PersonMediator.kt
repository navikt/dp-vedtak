package no.nav.dagpenger.vedtak.mediator

import mu.KotlinLogging
import no.nav.dagpenger.vedtak.mediator.mottak.SøknadBehandletMottak
import no.nav.dagpenger.vedtak.mediator.persistens.PersonRepository
import no.nav.dagpenger.vedtak.modell.Aktivitetslogg
import no.nav.dagpenger.vedtak.modell.Person
import no.nav.dagpenger.vedtak.modell.PersonIdentifikator.Companion.tilPersonIdentfikator
import no.nav.dagpenger.vedtak.modell.hendelser.Hendelse
import no.nav.dagpenger.vedtak.modell.hendelser.SøknadBehandletHendelse
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.withMDC

internal class PersonMediator(rapidsConnection: RapidsConnection, private val personRepository: PersonRepository) {

    init {
        SøknadBehandletMottak(rapidsConnection, this)
    }

    private companion object {
        val logger = KotlinLogging.logger { }
        val sikkerLogger = KotlinLogging.logger("tjenestekall")
    }

    fun håndter(søknadBehandletHendelse: SøknadBehandletHendelse) {
        behandle(søknadBehandletHendelse) { person ->
            person.håndter(søknadBehandletHendelse)
        }
    }

    private fun behandle(hendelse: Hendelse, håndter: (Person) -> Unit) = try {
        val person = hentEllerOpprettPerson(hendelse)
//        søknadObservers.forEach { søknadObserver ->
//            søknad.addObserver(søknadObserver)
//        }
        håndter(person)
        lagre(person)
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
            is SøknadBehandletHendelse -> Person(hendelse.ident().tilPersonIdentfikator())
            else -> {
                throw RuntimeException("har ikke informasjon om person")
            }
        }
    }

    private fun errorHandler(err: Exception, message: String, context: Map<String, String> = emptyMap()) {
        logger.error("alvorlig feil: ${err.message} (se sikkerlogg for melding)", err)
        withMDC(context) { sikkerLogger.error("alvorlig feil: ${err.message}\n\t$message", err) }
    }
}
