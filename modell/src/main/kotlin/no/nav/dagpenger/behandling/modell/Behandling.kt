package no.nav.dagpenger.behandling.modell

import no.nav.dagpenger.aktivitetslogg.Aktivitetskontekst
import no.nav.dagpenger.aktivitetslogg.SpesifikkKontekst
import no.nav.dagpenger.aktivitetslogg.Varselkode
import no.nav.dagpenger.aktivitetslogg.aktivitet.Hendelse
import no.nav.dagpenger.behandling.modell.Behandling.BehandlingTilstand.Companion.fraType
import no.nav.dagpenger.behandling.modell.hendelser.BehandlingAvbruttHendelse
import no.nav.dagpenger.behandling.modell.hendelser.BehandlingHendelse
import no.nav.dagpenger.behandling.modell.hendelser.OpplysningSvarHendelse
import no.nav.dagpenger.behandling.modell.hendelser.PersonHendelse
import no.nav.dagpenger.behandling.modell.hendelser.SøknadInnsendtHendelse
import no.nav.dagpenger.opplysning.LesbarOpplysninger
import no.nav.dagpenger.opplysning.Opplysning
import no.nav.dagpenger.opplysning.Opplysninger
import no.nav.dagpenger.opplysning.Regelkjøring
import no.nav.dagpenger.opplysning.verdier.Ulid
import java.util.UUID

class Behandling private constructor(
    val behandlingId: UUID,
    val behandler: BehandlingHendelse,
    aktiveOpplysninger: Opplysninger,
    val basertPå: List<Behandling> = emptyList(),
    private var tilstand: BehandlingTilstand,
) : Aktivitetskontekst {
    constructor(
        behandler: BehandlingHendelse,
        opplysninger: List<Opplysning<*>>,
        basertPå: List<Behandling> = emptyList(),
    ) : this(UUIDv7.ny(), behandler, Opplysninger(opplysninger), basertPå, UnderOpprettelse)

    private val tidligereOpplysninger: List<Opplysninger> = basertPå.map { it.opplysninger }
    private val opplysninger = aktiveOpplysninger + tidligereOpplysninger

    private val regelkjøring = Regelkjøring(behandler.skjedde, opplysninger, *behandler.regelsett().toTypedArray())

    companion object {
        fun rehydrer(
            behandlingId: UUID,
            behandler: BehandlingHendelse,
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

    fun håndter(hendelse: SøknadInnsendtHendelse) {
        hendelse.kontekst(this)
        tilstand.håndter(this, hendelse)
    }

    fun håndter(hendelse: OpplysningSvarHendelse) {
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

    override fun toSpesifikkKontekst() = BehandlingKontekst(behandlingId, behandler.kontekstMap())

    override fun equals(other: Any?) = other is Behandling && behandlingId == other.behandlingId

    override fun hashCode() = behandlingId.hashCode()

    data class BehandlingKontekst(val behandlingId: UUID, val behandlerKontekst: Map<String, String>) : SpesifikkKontekst("Behandling") {
        override val kontekstMap = mapOf("behandlingId" to behandlingId.toString()) + behandlerKontekst
    }

    private sealed interface BehandlingTilstand : Aktivitetskontekst {
        val type: TilstandType

        companion object {
            fun fraType(type: TilstandType) =
                when (type) {
                    TilstandType.UnderOpprettelse -> UnderOpprettelse
                    TilstandType.UnderBehandling -> UnderBehandling
                    TilstandType.ForslagTilVedtak -> ForslagTilVedtak
                    TilstandType.Avbrutt -> TODO()
                    TilstandType.Ferdig -> TODO()
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

        override fun toSpesifikkKontekst() = SpesifikkKontekst(type.name, emptyMap())
    }

    enum class TilstandType {
        UnderOpprettelse,
        UnderBehandling,
        ForslagTilVedtak,
        Avbrutt,
        Ferdig,
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
            hendelse.hendelse(BehandlingHendelser.behandling_opprettet, "Behandling opprettet")

            behandling.hvaTrengerViNå(hendelse)
            behandling.tilstand(UnderBehandling, hendelse)
        }
    }

    private data object UnderBehandling : BehandlingTilstand {
        override val type = TilstandType.UnderBehandling

        override fun håndter(
            behandling: Behandling,
            hendelse: SøknadInnsendtHendelse,
        ) {
            hendelse.kontekst(this)
            hendelse.info("Mottatt søknad og startet behandling")
            hendelse.varsel(Behandlingsvarsler.SØKNAD_MOTTATT)
            hendelse.hendelse(BehandlingHendelser.behandling_opprettet, "Behandling opprettet")

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
                BehandlingHendelser.forslag_til_vedtak,
                "Foreslår vedtak",
                mapOf(
                    "utfall" to behandling.opplysninger.finnOpplysning(behandling.behandler.avklarer()).verdi,
                    "harAvklart" to behandling.opplysninger.finnOpplysning(behandling.behandler.avklarer()).opplysningstype.navn,
                ),
            )
        }
    }

    private data object Avbrutt : BehandlingTilstand {
        override val type = TilstandType.Avbrutt

        override fun entering(
            behandling: Behandling,
            hendelse: PersonHendelse,
        ) {
            hendelse.kontekst(this)
            hendelse.info("Avbryter behandlingen")
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

    fun avbryt(hendelse: BehandlingAvbruttHendelse) {
        tilstand(Avbrutt, hendelse)
    }
}

@Suppress("ktlint:standard:class-naming")
object Behandlingsvarsler {
    data object SØKNAD_MOTTATT : Varselkode("Søknad mottatt - midlertidlig test av varsel")
}

// TODO: Vi bør ha bedre kontroll på navnene og kanskje henge sammen med behov?
@Suppress("ktlint:standard:enum-entry-name-case")
enum class BehandlingHendelser : Hendelse.Hendelsetype {
    behandling_opprettet,
    forslag_til_vedtak,
}
