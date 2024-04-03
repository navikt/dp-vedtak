package no.nav.dagpenger.behandling.modell

import no.nav.dagpenger.aktivitetslogg.Aktivitetskontekst
import no.nav.dagpenger.aktivitetslogg.SpesifikkKontekst
import no.nav.dagpenger.aktivitetslogg.Varselkode
import no.nav.dagpenger.aktivitetslogg.aktivitet.Hendelse
import no.nav.dagpenger.behandling.modell.Behandling.BehandlingTilstand.Companion.fraType
import no.nav.dagpenger.behandling.modell.BehandlingHendelser.VedtakFattetHendelse
import no.nav.dagpenger.behandling.modell.hendelser.AvbrytBehandlingHendelse
import no.nav.dagpenger.behandling.modell.hendelser.ForslagGodkjentHendelse
import no.nav.dagpenger.behandling.modell.hendelser.OpplysningSvarHendelse
import no.nav.dagpenger.behandling.modell.hendelser.PersonHendelse
import no.nav.dagpenger.behandling.modell.hendelser.StartHendelse
import no.nav.dagpenger.behandling.modell.hendelser.SøknadInnsendtHendelse
import no.nav.dagpenger.opplysning.Hypotese
import no.nav.dagpenger.opplysning.LesbarOpplysninger
import no.nav.dagpenger.opplysning.Opplysning
import no.nav.dagpenger.opplysning.Opplysninger
import no.nav.dagpenger.opplysning.Regelkjøring
import no.nav.dagpenger.opplysning.verdier.Ulid
import java.util.UUID

class Behandling private constructor(
    val behandlingId: UUID,
    val behandler: StartHendelse,
    aktiveOpplysninger: Opplysninger,
    val basertPå: List<Behandling> = emptyList(),
    private var tilstand: BehandlingTilstand,
) : Aktivitetskontekst, BehandlingHåndter {
    constructor(
        behandler: StartHendelse,
        opplysninger: List<Opplysning<*>>,
        basertPå: List<Behandling> = emptyList(),
    ) : this(UUIDv7.ny(), behandler, Opplysninger(opplysninger), basertPå, UnderOpprettelse)

    private val observatører = mutableListOf<BehandlingObservatør>()

    private val tidligereOpplysninger: List<Opplysninger> = basertPå.map { it.opplysninger }
    private val opplysninger = aktiveOpplysninger + tidligereOpplysninger

    private val regelkjøring = Regelkjøring(behandler.skjedde, opplysninger, *behandler.regelsett().toTypedArray())

    companion object {
        fun rehydrer(
            behandlingId: UUID,
            behandler: StartHendelse,
            aktiveOpplysninger: Opplysninger,
            basertPå: List<Behandling> = emptyList(),
            tilstand: TilstandType,
        ) = Behandling(behandlingId, behandler, aktiveOpplysninger, basertPå, fraType(tilstand))

        fun List<Behandling>.finn(behandlingId: UUID) =
            try {
                single { it.behandlingId == behandlingId }
            } catch (e: IllegalArgumentException) {
                throw IllegalArgumentException("Fant flere behandlinger med samme id, id=$behandlingId", e)
            }
    }

    fun tilstand() = tilstand.type

    fun opplysninger(): LesbarOpplysninger = opplysninger

    private fun informasjonsbehov() = regelkjøring.informasjonsbehov(behandler.avklarer())

    override fun håndter(hendelse: SøknadInnsendtHendelse) {
        hendelse.kontekst(this)
        tilstand.håndter(this, hendelse)
    }

    override fun håndter(hendelse: OpplysningSvarHendelse) {
        hendelse.kontekst(this)
        tilstand.håndter(this, hendelse)
    }

    override fun håndter(hendelse: AvbrytBehandlingHendelse) {
        hendelse.kontekst(this)
        tilstand.håndter(this, hendelse)
    }

    override fun håndter(hendelse: ForslagGodkjentHendelse) {
        hendelse.kontekst(this)
        tilstand.håndter(this, hendelse)
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
                        // TODO: Midlertidlig hack for å få med søknadId for gamle behovløsere
                        mapOf(
                            "InnsendtSøknadsId" to mapOf("urn" to "urn:soknad:${behandler.eksternId.id}"),
                            "søknad_uuid" to behandler.eksternId.id.toString(),
                        ),
            )
        }

    fun registrer(observatør: BehandlingObservatør) {
        observatører.add(observatør)
    }

    override fun toSpesifikkKontekst() = BehandlingKontekst(behandlingId, behandler.kontekstMap())

    override fun equals(other: Any?) = other is Behandling && behandlingId == other.behandlingId

    override fun hashCode() = behandlingId.hashCode()

    data class BehandlingKontekst(val behandlingId: UUID, val behandlerKontekst: Map<String, String>) : SpesifikkKontekst("Behandling") {
        override val kontekstMap = mapOf("behandlingId" to behandlingId.toString()) + behandlerKontekst
    }

    enum class TilstandType {
        UnderOpprettelse,
        UnderBehandling,
        ForslagTilVedtak,
        Avbrutt,
        Ferdig,
    }

    private sealed interface BehandlingTilstand : Aktivitetskontekst {
        val type: TilstandType

        companion object {
            fun fraType(type: TilstandType) =
                when (type) {
                    TilstandType.UnderOpprettelse -> UnderOpprettelse
                    TilstandType.UnderBehandling -> UnderBehandling
                    TilstandType.ForslagTilVedtak -> ForslagTilVedtak
                    TilstandType.Avbrutt -> Avbrutt
                    TilstandType.Ferdig -> Ferdig
                }
        }

        fun entering(
            behandling: Behandling,
            hendelse: PersonHendelse,
        ) {
        }

        fun håndter(
            behandling: Behandling,
            hendelse: SøknadInnsendtHendelse,
        ) {
            throw IllegalStateException(
                "Kan ikke håndtere hendelse ${hendelse.javaClass.simpleName} i tilstand ${this.javaClass.simpleName}",
            )
        }

        fun håndter(
            behandling: Behandling,
            hendelse: OpplysningSvarHendelse,
        ) {
            throw IllegalStateException(
                "Kan ikke håndtere hendelse ${hendelse.javaClass.simpleName} i tilstand ${this.javaClass.simpleName}",
            )
        }

        fun håndter(
            behandling: Behandling,
            hendelse: AvbrytBehandlingHendelse,
        ) {
            hendelse.info("Avbryter behandlingen")
            behandling.tilstand(Avbrutt, hendelse)
        }

        fun håndter(
            behandling: Behandling,
            hendelse: ForslagGodkjentHendelse,
        ) {
            throw IllegalStateException(
                "Kan ikke håndtere hendelse ${hendelse.javaClass.simpleName} i tilstand ${this.javaClass.simpleName}",
            )
        }

        override fun toSpesifikkKontekst() = SpesifikkKontekst(type.name, emptyMap())
    }

    private data object UnderOpprettelse : BehandlingTilstand {
        override val type = TilstandType.UnderOpprettelse

        override fun håndter(
            behandling: Behandling,
            hendelse: SøknadInnsendtHendelse,
        ) {
            hendelse.kontekst(this)
            hendelse.info("Mottatt søknad og startet behandling")
            hendelse.varsel(Behandlingsvarsler.SØKNAD_MOTTATT)
            hendelse.hendelse(BehandlingHendelser.BehandlingOpprettetHendelse, "Behandling opprettet")

            behandling.hvaTrengerViNå(hendelse)
            behandling.tilstand(UnderBehandling, hendelse)
        }
    }

    private data object UnderBehandling : BehandlingTilstand {
        override val type = TilstandType.UnderBehandling

        override fun entering(
            behandling: Behandling,
            hendelse: PersonHendelse,
        ) {
            behandling.observatører.forEach { it.behandlingStartet() }
        }

        override fun håndter(
            behandling: Behandling,
            hendelse: SøknadInnsendtHendelse,
        ) {
            hendelse.kontekst(this)
            hendelse.info("Mottatt søknad og startet behandling")
            hendelse.varsel(Behandlingsvarsler.SØKNAD_MOTTATT)
            hendelse.hendelse(BehandlingHendelser.BehandlingOpprettetHendelse, "Behandling opprettet")

            behandling.hvaTrengerViNå(hendelse)
        }

        override fun håndter(
            behandling: Behandling,
            hendelse: OpplysningSvarHendelse,
        ) {
            hendelse.kontekst(this)
            hendelse.opplysninger.forEach { opplysning ->
                behandling.opplysninger.leggTil(opplysning.opplysning())
            }
            val trenger = behandling.hvaTrengerViNå(hendelse)

            if (trenger.isEmpty()) {
                behandling.tilstand(ForslagTilVedtak, hendelse)
            }
        }
    }

    private data object ForslagTilVedtak : BehandlingTilstand {
        override val type = TilstandType.ForslagTilVedtak

        override fun entering(
            behandling: Behandling,
            hendelse: PersonHendelse,
        ) {
            hendelse.kontekst(this)
            hendelse.info("Alle opplysninger mottatt, lager forslag til vedtak")
            hendelse.hendelse(
                BehandlingHendelser.ForslagTilVedtakHendelse,
                "Foreslår vedtak",
                mapOf(
                    "utfall" to behandling.opplysninger.finnOpplysning(behandling.behandler.avklarer()).verdi,
                    "harAvklart" to behandling.opplysninger.finnOpplysning(behandling.behandler.avklarer()).opplysningstype.navn,
                ),
            )
            behandling.observatører.forEach { it.forslagTilVedtak() }
        }

        override fun håndter(
            behandling: Behandling,
            hendelse: ForslagGodkjentHendelse,
        ) {
            hendelse.kontekst(this)
            hendelse.info("Forslag til vedtak godkjent")
            // TODO: Hva mer gjør vi når vi har godkjent forslaget?
            // Sjekke aksjonspunkter/varsel/hypoteser?
            if (behandling.opplysninger.finnAlle().any { it is Hypotese<*> }) {
                // TODO: Vi bør sannsynligvis gjøre dette
                // throw IllegalStateException("Forslaget inneholder hypoteser, kan ikke godkjennes")
            }
            // TODO: Dette er vel strengt tatt ikke vedtak fattet?
            hendelse.hendelse(
                VedtakFattetHendelse,
                "Vedtak fattet",
                mapOf(
                    "utfall" to behandling.opplysninger.finnOpplysning(behandling.behandler.avklarer()).verdi,
                    "harAvklart" to behandling.opplysninger.finnOpplysning(behandling.behandler.avklarer()).opplysningstype.navn,
                    "opplysninger" to behandling.opplysninger.finnAlle(),
                ),
            )
            behandling.tilstand(Ferdig, hendelse)
        }
    }

    private data object Avbrutt : BehandlingTilstand {
        override val type = TilstandType.Avbrutt

        override fun entering(
            behandling: Behandling,
            hendelse: PersonHendelse,
        ) {
            behandling.observatører.forEach { it.avbrutt() }
        }

        override fun håndter(
            behandling: Behandling,
            hendelse: AvbrytBehandlingHendelse,
        ) { // No-op
        }

        override fun håndter(
            behandling: Behandling,
            hendelse: OpplysningSvarHendelse,
        ) {
            hendelse.kontekst(this)
            hendelse.info("Behandlingen er avbrutt, ignorerer opplysningssvar")
        }
    }

    private data object Ferdig : BehandlingTilstand {
        override val type = TilstandType.Ferdig

        override fun entering(
            behandling: Behandling,
            hendelse: PersonHendelse,
        ) {
            behandling.observatører.forEach { it.ferdig() }
        }

        override fun håndter(
            behandling: Behandling,
            hendelse: AvbrytBehandlingHendelse,
        ) {
            throw IllegalStateException("Kan ikke avbryte en ferdig behandling")
        }
    }

    private fun tilstand(
        nyTilstand: BehandlingTilstand,
        hendelse: PersonHendelse,
    ) {
        if (tilstand.type == nyTilstand.type) return

        val forrigeTilstand = tilstand
        tilstand = nyTilstand
        hendelse.kontekst(tilstand)
        tilstand.entering(this, hendelse)
    }
}

interface BehandlingObservatør {
    fun behandlingStartet() {}

    fun forslagTilVedtak() {}

    fun avbrutt() {}

    fun ferdig() {}
}

@Suppress("ktlint:standard:class-naming")
object Behandlingsvarsler {
    data object SØKNAD_MOTTATT : Varselkode("Søknad mottatt - midlertidlig test av varsel")
}

// TODO: Vi bør ha bedre kontroll på navnene og kanskje henge sammen med behov?
@Suppress("ktlint:standard:enum-entry-name-case")
sealed class BehandlingHendelser(override val name: String) : Hendelse.Hendelsetype {
    data object BehandlingOpprettetHendelse : BehandlingHendelser("behandling_opprettet")

    data object ForslagTilVedtakHendelse : BehandlingHendelser("forslag_til_vedtak")

    data object VedtakFattetHendelse : BehandlingHendelser("vedtak_fattet")
}
