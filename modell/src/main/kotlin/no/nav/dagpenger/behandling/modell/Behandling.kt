package no.nav.dagpenger.behandling.modell

import no.nav.dagpenger.aktivitetslogg.Aktivitet
import no.nav.dagpenger.aktivitetslogg.Aktivitetskontekst
import no.nav.dagpenger.aktivitetslogg.SpesifikkKontekst
import no.nav.dagpenger.behandling.modell.hendelser.OpplysningSvarHendelse
import no.nav.dagpenger.behandling.modell.hendelser.PersonHendelse
import no.nav.dagpenger.behandling.modell.hendelser.SøkerHendelse
import no.nav.dagpenger.behandling.modell.hendelser.SøknadInnsendtHendelse
import no.nav.dagpenger.opplysning.Opplysninger
import no.nav.dagpenger.opplysning.Regelkjøring
import no.nav.dagpenger.opplysning.Regelsett
import no.nav.dagpenger.regel.RettTilDagpenger
import java.util.UUID

class Behandling private constructor(
    val behandlingId: UUID,
    private val behandler: SøkerHendelse,
    private val opplysninger: Opplysninger,
    vararg regelsett: Regelsett,
) : Aktivitetskontekst {
    constructor(
        behandler: SøkerHendelse,
        opplysninger: Opplysninger,
        vararg regelsett: Regelsett,
    ) : this(UUIDv7.ny(), behandler, opplysninger, *regelsett)

    private val regelkjøring = Regelkjøring(behandler.gjelderDato, opplysninger, *regelsett)

    // @todo: Vi trenger noe tilsvarende visitor pattern for å hente opplysninger fra utsiden
    fun opplysninger() = opplysninger.opplysninger()

    private fun informasjonsbehov() = regelkjøring.informasjonsbehov(RettTilDagpenger.kravPåDagpenger)

    fun håndter(hendelse: SøknadInnsendtHendelse) {
        hendelse.kontekst(this)
        hvaTrengerViNå(hendelse)
    }

    fun håndter(hendelse: OpplysningSvarHendelse) {
        hendelse.kontekst(this)
        hendelse.opplysninger.forEach { opplysning ->
            opplysninger.leggTil(opplysning.opplysning())
        }
        hvaTrengerViNå(hendelse)
    }

    private fun hvaTrengerViNå(hendelse: PersonHendelse) {
        informasjonsbehov().forEach { (behov, avhengigheter) ->
            hendelse.behov(
                type = OpplysningBehov(behov.id),
                melding = "Trenger en opplysning",
                detaljer =
                    avhengigheter.associate {
                            av ->
                        av.opplysningstype.id to av.verdi
                        // @todo: Denne skal bort så fort vi har en behovløser vi kan være enige med innbyggerflate om. Tilpasset 'SøknadInnsendtTidspunktTjeneste' for å kunne teste
                    } + mapOf("Søknadstidspunkt" to mapOf("søknad_uuid" to behandler.søknadId.toString())),
            )
        }
    }

    override fun toSpesifikkKontekst() = SpesifikkKontekst("Behandling", mapOf("behandlingId" to behandlingId.toString()))
}

data class OpplysningBehov(override val name: String) : Aktivitet.Behov.Behovtype
