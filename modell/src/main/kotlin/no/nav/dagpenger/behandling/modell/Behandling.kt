package no.nav.dagpenger.behandling.modell

import no.nav.dagpenger.aktivitetslogg.Aktivitet
import no.nav.dagpenger.aktivitetslogg.Aktivitetskontekst
import no.nav.dagpenger.aktivitetslogg.SpesifikkKontekst
import no.nav.dagpenger.aktivitetslogg.Varselkode
import no.nav.dagpenger.behandling.modell.BehandlingObservatør.BehandlingAvsluttet
import no.nav.dagpenger.behandling.modell.hendelser.OpplysningSvarHendelse
import no.nav.dagpenger.behandling.modell.hendelser.PersonHendelse
import no.nav.dagpenger.behandling.modell.hendelser.SøkerHendelse
import no.nav.dagpenger.behandling.modell.hendelser.SøknadInnsendtHendelse
import no.nav.dagpenger.opplysning.LesbarOpplysninger
import no.nav.dagpenger.opplysning.Opplysning
import no.nav.dagpenger.opplysning.Opplysninger
import no.nav.dagpenger.opplysning.Regelkjøring
import no.nav.dagpenger.opplysning.Regelsett
import no.nav.dagpenger.opplysning.verdier.Ulid
import no.nav.dagpenger.regel.RettTilDagpenger
import java.util.UUID

class Behandling private constructor(
    val behandlingId: UUID,
    private val behandler: SøkerHendelse,
    aktiveOpplysninger: List<Opplysning<*>> = emptyList(),
    basertPå: List<Behandling> = emptyList(),
    vararg regelsett: Regelsett,
) : Aktivitetskontekst {
    constructor(
        behandler: SøkerHendelse,
        opplysninger: List<Opplysning<*>>,
        vararg regelsett: Regelsett,
        basertPå: List<Behandling> = emptyList(),
    ) : this(UUIDv7.ny(), behandler, opplysninger, basertPå, *regelsett)

    private val tidligereOpplysninger: List<Opplysninger> = basertPå.map { it.opplysninger }
    private val opplysninger = Opplysninger(aktiveOpplysninger, tidligereOpplysninger)

    private val regelkjøring = Regelkjøring(behandler.gjelderDato, opplysninger, *regelsett)
    private val observatører = mutableListOf<BehandlingObservatør>()

    internal fun leggTilObservatør(observatør: BehandlingObservatør) {
        observatører.add(observatør)
    }

    fun opplysninger(): LesbarOpplysninger = opplysninger

    private fun informasjonsbehov() = regelkjøring.informasjonsbehov(RettTilDagpenger.kravPåDagpenger)

    fun håndter(hendelse: SøknadInnsendtHendelse) {
        hendelse.kontekst(this)
        hendelse.info("Mottatt søknad og startet behandling")
        hendelse.varsel(Behandlingsvarsler.SØKNAD_MOTTATT)

        observatører.forEach {
            it.behandlingOpprettet(
                BehandlingObservatør.BehandlingOpprettet(hendelse.ident, behandlingId, hendelse.søknadId),
            )
        }
        hvaTrengerViNå(hendelse)
    }

    fun håndter(hendelse: OpplysningSvarHendelse) {
        hendelse.kontekst(this)
        hendelse.opplysninger.forEach { opplysning ->
            opplysninger.leggTil(opplysning.opplysning())
        }
        val trenger = hvaTrengerViNå(hendelse)

        if (trenger.isEmpty()) {
            // TODO: Tilstand?
            hendelse.info("Alle opplysninger mottatt")
            observatører.forEach {
                it.behandlingAvsluttet(BehandlingAvsluttet(behandler.ident, behandlingId, behandler.søknadId))
            }
        }
    }

    private fun hvaTrengerViNå(hendelse: PersonHendelse) =
        informasjonsbehov().onEach { (behov, avhengigheter) ->
            hendelse.behov(
                type = OpplysningBehov(behov.id),
                melding = "Trenger en opplysning (${behov.id})",
                detaljer =
                    avhengigheter.associate { avhengighet ->
                        val verdi =
                            when (avhengighet.verdi) {
                                is Ulid -> (avhengighet.verdi as Ulid).verdi
                                else -> avhengighet.verdi
                            }
                        avhengighet.opplysningstype.id to verdi
                    } +
                        // @todo: Denne skal bort så fort vi har en behovløser vi kan være enige med innbyggerflate om. Tilpasset 'SøknadInnsendtTidspunktTjeneste' for å kunne teste
                        mapOf("søknad_uuid" to behandler.søknadId.toString()),
            )
        }

    override fun toSpesifikkKontekst() = SpesifikkKontekst("Behandling", mapOf("behandlingId" to behandlingId.toString()))
}

data class OpplysningBehov(override val name: String) : Aktivitet.Behov.Behovtype

object Behandlingsvarsler {
    @Suppress("ClassName")
    data object SØKNAD_MOTTATT : Varselkode2("Søknad mottatt - midlertidlig test av varsel")
}

// TODO: Midlertidlig bridge til vi får fikset aktivitetsloggen
abstract class Varselkode2(override val varseltekst: String) : Varselkode() {
    override fun toString() = "${this::class.java.simpleName}: $varseltekst"
}
