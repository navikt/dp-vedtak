package no.nav.dagpenger.behandling.modell

import no.nav.dagpenger.aktivitetslogg.Aktivitetskontekst
import no.nav.dagpenger.aktivitetslogg.SpesifikkKontekst
import no.nav.dagpenger.aktivitetslogg.Varselkode
import no.nav.dagpenger.aktivitetslogg.aktivitet.Hendelse
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
    aktiveOpplysninger: List<Opplysning<*>> = emptyList(),
    val basertPå: List<Behandling> = emptyList(),
) : Aktivitetskontekst {
    constructor(
        behandler: BehandlingHendelse,
        opplysninger: List<Opplysning<*>>,
        basertPå: List<Behandling> = emptyList(),
    ) : this(UUIDv7.ny(), behandler, opplysninger, basertPå)

    private val tidligereOpplysninger: List<Opplysninger> = basertPå.map { it.opplysninger }
    private val opplysninger = Opplysninger(aktiveOpplysninger, tidligereOpplysninger)

    private val regelkjøring = Regelkjøring(behandler.skjedde, opplysninger, *behandler.regelsett().toTypedArray())

    companion object {
        fun rehydrer(
            behandlingId: UUID,
            behandler: BehandlingHendelse,
            aktiveOpplysninger: List<Opplysning<*>> = emptyList(),
            basertPå: List<Behandling> = emptyList(),
        ) = Behandling(behandlingId, behandler, aktiveOpplysninger, basertPå)

        fun List<Behandling>.finn(behandlingId: UUID) =
            try {
                single { it.behandlingId == behandlingId }
            } catch (e: IllegalArgumentException) {
                throw IllegalArgumentException("Fant flere behandlinger med samme id, id=$behandlingId", e)
            }
    }

    fun opplysninger(): LesbarOpplysninger = opplysninger

    private fun informasjonsbehov() = regelkjøring.informasjonsbehov(behandler.avklarer())

    fun håndter(hendelse: SøknadInnsendtHendelse) {
        hendelse.kontekst(this)
        hendelse.info("Mottatt søknad og startet behandling")
        hendelse.varsel(Behandlingsvarsler.SØKNAD_MOTTATT)
        hendelse.hendelse(BehandlingHendelser.behandling_opprettet, "Behandling opprettet")

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
            hendelse.hendelse(
                BehandlingHendelser.forslag_til_vedtak,
                "Foreslår vedtak",
                mapOf(
                    "utfall" to opplysninger.finnOpplysning(behandler.avklarer()).verdi,
                    "harAvklart" to opplysninger.finnOpplysning(behandler.avklarer()).opplysningstype.navn,
                ),
            )
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
                    },
            )
        }

    override fun toSpesifikkKontekst() = BehandlingKontekst(behandlingId, behandler.kontekstMap())

    // TODO: VIl helst ikke ha søknadId inn her
    data class BehandlingKontekst(val behandlingId: UUID, val behandlerKontekst: Map<String, String>) : SpesifikkKontekst("Behandling") {
        override val kontekstMap = mapOf("behandlingId" to behandlingId.toString()) + behandlerKontekst
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
