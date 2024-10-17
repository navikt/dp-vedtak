package no.nav.dagpenger.behandling.modell

import no.nav.dagpenger.aktivitetslogg.Aktivitetskontekst
import no.nav.dagpenger.aktivitetslogg.SpesifikkKontekst
import no.nav.dagpenger.aktivitetslogg.aktivitet.Hendelse
import no.nav.dagpenger.avklaring.Avklaring
import no.nav.dagpenger.avklaring.Avklaringer
import no.nav.dagpenger.avklaring.Avklaringkode
import no.nav.dagpenger.behandling.modell.Behandling.BehandlingTilstand.Companion.fraType
import no.nav.dagpenger.behandling.modell.BehandlingHendelser.AvklaringLukketHendelse
import no.nav.dagpenger.behandling.modell.PersonObservatør.PersonEvent
import no.nav.dagpenger.behandling.modell.hendelser.AvbrytBehandlingHendelse
import no.nav.dagpenger.behandling.modell.hendelser.AvklaringIkkeRelevantHendelse
import no.nav.dagpenger.behandling.modell.hendelser.EksternId
import no.nav.dagpenger.behandling.modell.hendelser.ForslagGodkjentHendelse
import no.nav.dagpenger.behandling.modell.hendelser.NyOpplysningHendelse
import no.nav.dagpenger.behandling.modell.hendelser.PersonHendelse
import no.nav.dagpenger.behandling.modell.hendelser.PåminnelseHendelse
import no.nav.dagpenger.behandling.modell.hendelser.StartHendelse
import no.nav.dagpenger.behandling.modell.hendelser.SøknadInnsendtHendelse
import no.nav.dagpenger.opplysning.Hypotese
import no.nav.dagpenger.opplysning.Informasjonsbehov
import no.nav.dagpenger.opplysning.LesbarOpplysninger
import no.nav.dagpenger.opplysning.Opplysning
import no.nav.dagpenger.opplysning.Opplysninger
import no.nav.dagpenger.opplysning.Regelkjøring
import no.nav.dagpenger.opplysning.Saksbehandlerkilde
import no.nav.dagpenger.opplysning.regel.Regel
import no.nav.dagpenger.opplysning.verdier.Ulid
import no.nav.dagpenger.regel.KravPåDagpenger
import no.nav.dagpenger.regel.Minsteinntekt.minsteinntekt
import no.nav.dagpenger.regel.støtterInnvilgelseOpplysningstype
import no.nav.dagpenger.uuid.UUIDv7
import java.time.Duration
import java.time.LocalDateTime
import java.util.UUID

class Behandling private constructor(
    val behandlingId: UUID,
    val behandler: StartHendelse,
    gjeldendeOpplysninger: Opplysninger,
    val basertPå: List<Behandling> = emptyList(),
    private var tilstand: BehandlingTilstand,
    avklaringer: List<Avklaring>,
) : Aktivitetskontekst,
    BehandlingHåndter {
    constructor(
        behandler: StartHendelse,
        opplysninger: List<Opplysning<*>>,
        basertPå: List<Behandling> = emptyList(),
    ) : this(UUIDv7.ny(), behandler, Opplysninger(opplysninger), basertPå, UnderOpprettelse(LocalDateTime.now()), emptyList())

    init {
        require(basertPå.all { it.tilstand is Ferdig }) {
            "Kan ikke basere en ny behandling på en som ikke er ferdig"
        }
    }

    private val observatører = mutableListOf<BehandlingObservatør>()

    private val tidligereOpplysninger: List<Opplysninger> = basertPå.map { it.opplysninger }
    private val opplysninger: Opplysninger = (gjeldendeOpplysninger + tidligereOpplysninger)

    private val regelkjøring: Regelkjøring get() = behandler.regelkjøring(opplysninger)

    private val avklaringer = Avklaringer(behandler.kontrollpunkter(), avklaringer)

    fun avklaringer() = avklaringer.avklaringer(opplysninger.forDato(behandler.skjedde))

    fun erAutomatiskBehandlet() = avklaringer().all { it.erAvklart() || it.erAvbrutt() }

    fun aktiveAvklaringer() = avklaringer.måAvklares(opplysninger.forDato(behandler.skjedde))

    companion object {
        fun rehydrer(
            behandlingId: UUID,
            behandler: StartHendelse,
            gjeldendeOpplysninger: Opplysninger,
            basertPå: List<Behandling> = emptyList(),
            tilstand: TilstandType,
            sistEndretTilstand: LocalDateTime,
            avklaringer: List<Avklaring>,
        ) = Behandling(
            behandlingId,
            behandler,
            gjeldendeOpplysninger,
            basertPå,
            fraType(tilstand, sistEndretTilstand),
            avklaringer,
        )

        fun List<Behandling>.finn(behandlingId: UUID) =
            try {
                single { it.behandlingId == behandlingId }
            } catch (e: IllegalArgumentException) {
                throw IllegalArgumentException("Fant flere behandlinger med samme id, id=$behandlingId", e)
            }

        private val manglerKonklusjonAvklaring =
            Avklaringkode(
                "ManglerKonklusjon",
                "Mangler konklusjon",
                """Vi klarer ikke å finne en konklusjon på behandlingen. Sannsynligvis er ikke alle vilkårene oppfylt 
                                |på prøvingsdatoen, eller vi mangler opplysninger.
                """.trimMargin(),
                false,
            )
    }

    fun tilstand() = Pair(tilstand.type, tilstand.opprettet)

    fun opplysninger(): LesbarOpplysninger = opplysninger

    override fun håndter(hendelse: SøknadInnsendtHendelse) {
        hendelse.kontekst(this)
        tilstand.håndter(this, hendelse)
    }

    override fun håndter(hendelse: AvklaringIkkeRelevantHendelse) {
        hendelse.kontekst(this)
        tilstand.håndter(this, hendelse)
    }

    override fun håndter(hendelse: NyOpplysningHendelse) {
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

    override fun håndter(hendelse: PåminnelseHendelse) {
        hendelse.kontekst(this)
        tilstand.håndter(this, hendelse)
    }

    fun registrer(observatør: BehandlingObservatør) {
        observatører.add(observatør)
    }

    override fun toSpesifikkKontekst() = BehandlingKontekst(behandlingId, behandler.kontekstMap())

    override fun equals(other: Any?) = other is Behandling && behandlingId == other.behandlingId

    override fun hashCode() = behandlingId.hashCode()

    data class BehandlingKontekst(
        val behandlingId: UUID,
        val behandlerKontekst: Map<String, String>,
    ) : SpesifikkKontekst("Behandling") {
        override val kontekstMap = mapOf("behandlingId" to behandlingId.toString()) + behandlerKontekst
    }

    enum class TilstandType {
        UnderOpprettelse,
        UnderBehandling,
        ForslagTilVedtak,
        Avbrutt,
        Ferdig,
        Redigert,
    }

    private sealed interface BehandlingTilstand : Aktivitetskontekst {
        val type: TilstandType
        val opprettet: LocalDateTime

        val forventetFerdig: LocalDateTime get() = LocalDateTime.MAX

        companion object {
            fun fraType(
                type: TilstandType,
                opprettet: LocalDateTime,
            ) = when (type) {
                TilstandType.UnderOpprettelse -> UnderOpprettelse(opprettet)
                TilstandType.UnderBehandling -> UnderBehandling(opprettet)
                TilstandType.ForslagTilVedtak -> ForslagTilVedtak(opprettet)
                TilstandType.Avbrutt -> Avbrutt(opprettet)
                TilstandType.Ferdig -> Ferdig(opprettet)
                TilstandType.Redigert -> Redigert(opprettet)
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
        ): Unit =
            throw IllegalStateException(
                "Kan ikke håndtere hendelse ${hendelse.javaClass.simpleName} i tilstand ${this.javaClass.simpleName}",
            )

        fun håndter(
            behandling: Behandling,
            hendelse: NyOpplysningHendelse,
        ): Unit =
            throw IllegalStateException(
                "Kan ikke håndtere hendelse ${hendelse.javaClass.simpleName} i tilstand ${this.javaClass.simpleName}",
            )

        fun håndter(
            behandling: Behandling,
            hendelse: AvbrytBehandlingHendelse,
        ) {
            hendelse.info("Avbryter behandlingen")
            behandling.tilstand(Avbrutt(årsak = hendelse.årsak), hendelse)
        }

        fun håndter(
            behandling: Behandling,
            hendelse: ForslagGodkjentHendelse,
        ): Unit =
            throw IllegalStateException(
                "Kan ikke håndtere hendelse ${hendelse.javaClass.simpleName} i tilstand ${this.javaClass.simpleName}",
            )

        fun håndter(
            behandling: Behandling,
            hendelse: AvklaringIkkeRelevantHendelse,
        ): Unit =
            throw IllegalStateException(
                "Kan ikke håndtere hendelse ${hendelse.javaClass.simpleName} i tilstand ${this.javaClass.simpleName}",
            )

        fun håndter(
            behandling: Behandling,
            hendelse: PåminnelseHendelse,
        ) {
            hendelse.info("Behandlingen mottok påminnelse, men tilstanden støtter ikke dette")
        }

        fun leaving(
            behandling: Behandling,
            hendelse: PersonHendelse,
        ) {
        }

        override fun toSpesifikkKontekst() =
            SpesifikkKontekst(
                type.name,
                mapOf(
                    "opprettet" to opprettet.toString(),
                ),
            )
    }

    private data class UnderOpprettelse(
        override val opprettet: LocalDateTime = LocalDateTime.now(),
    ) : BehandlingTilstand {
        override val type = TilstandType.UnderOpprettelse

        override fun håndter(
            behandling: Behandling,
            hendelse: SøknadInnsendtHendelse,
        ) {
            hendelse.kontekst(this)
            hendelse.info("Mottatt søknad og startet behandling")
            behandling.observatører.forEach { it.behandlingStartet() }
            hendelse.hendelse(BehandlingHendelser.BehandlingOpprettetHendelse, "Behandling opprettet")

            behandling.tilstand(UnderBehandling(), hendelse)
        }
    }

    private data class UnderBehandling(
        override val opprettet: LocalDateTime = LocalDateTime.now(),
    ) : BehandlingTilstand {
        override val type = TilstandType.UnderBehandling
        override val forventetFerdig: LocalDateTime get() = opprettet.plusHours(1)

        override fun entering(
            behandling: Behandling,
            hendelse: PersonHendelse,
        ) {
            behandling.observatører.forEach { it.behandlingStartet() }
            val regelkjøringsrapport = behandling.regelkjøring.evaluer()
            hendelse.lagBehov(regelkjøringsrapport.informasjonsbehov)

            if (regelkjøringsrapport.erFerdig()) {
                behandling.tilstand(ForslagTilVedtak(), hendelse)
            }
        }

        override fun håndter(
            behandling: Behandling,
            hendelse: PåminnelseHendelse,
        ) {
            hendelse.kontekst(this)
            val rapport = behandling.regelkjøring.evaluer()
            if (rapport.erFerdig()) {
                hendelse.logiskFeil("Behandlingen er ferdig men vi er fortsatt i ${this.type.name}")
            }
            hendelse.lagBehov(rapport.informasjonsbehov)
        }

        override fun håndter(
            behandling: Behandling,
            hendelse: NyOpplysningHendelse,
        ) {
            hendelse.kontekst(this)
            hendelse.leggTil(behandling.opplysninger)

            // Kjør regelkjøring for alle opplysninger
            val rapport = behandling.regelkjøring.evaluer()
            rapport.kjørteRegler.forEach { regel: Regel<*> ->
                hendelse.info(regel.toString())
            }

            val støtterInnvilgelse =
                behandling.opplysninger.har(støtterInnvilgelseOpplysningstype) &&
                    behandling.opplysninger.finnOpplysning(støtterInnvilgelseOpplysningstype).verdi

            if (!støtterInnvilgelse) {
                // TODO: Dette faller bort når vi sjekker alt
                val kravPåDagpenger =
                    behandling.opplysninger.har(KravPåDagpenger.kravPåDagpenger) &&
                        behandling.opplysninger.finnOpplysning(KravPåDagpenger.kravPåDagpenger).verdi
                if (kravPåDagpenger) {
                    hendelse.info("Behandling fører ikke til avslag, det støtter vi ikke enda")
                    behandling.tilstand(Avbrutt(årsak = "Førte ikke til avslag"), hendelse)
                    return
                }

                // TODO: Dette faller bort når vi sjekker alt
                val kravTilInntekt =
                    behandling.opplysninger.har(minsteinntekt) &&
                        behandling.opplysninger.finnOpplysning(minsteinntekt).verdi
                if (kravTilInntekt) {
                    hendelse.info("Behandling er avslag, men kravet til inntekt er oppfylt, det støtter vi ikke enda")
                    behandling.tilstand(Avbrutt(årsak = "Førte ikke til avslag på grunn av inntekt"), hendelse)
                    return
                }
            }

            hendelse.lagBehov(rapport.informasjonsbehov)

            if (rapport.erFerdig()) {
                if (!behandling.opplysninger.har(behandling.behandler.avklarer(behandling.opplysninger))) {
                    behandling.avklaringer.nyAvklaring(manglerKonklusjonAvklaring)
                }

                if (behandling.aktiveAvklaringer().isEmpty()) {
                    hendelse.info("Har ingen aktive avklaringer, går videre til vedtak.")
                    behandling.tilstand(Ferdig(), hendelse)
                    return
                }

                // Her vet vi at det skal være avslag på grunn av minste arbeidsinntekt.
                hendelse.info("Har aktive avklaringer, går videre til forslag til vedtak.")
                behandling.tilstand(ForslagTilVedtak(), hendelse)
            }
        }

        override fun håndter(
            behandling: Behandling,
            hendelse: AvklaringIkkeRelevantHendelse,
        ) {
            hendelse.kontekst(this)
            if (behandling.avklaringer.avklar(hendelse.avklaringId, hendelse.kilde)) {
                hendelse.info("Avklaring er ikke lenger relevant")
            }
        }
    }

    private data class ForslagTilVedtak(
        override val opprettet: LocalDateTime = LocalDateTime.now(),
    ) : BehandlingTilstand {
        override val type = TilstandType.ForslagTilVedtak

        override fun entering(
            behandling: Behandling,
            hendelse: PersonHendelse,
        ) {
            hendelse.kontekst(this)
            hendelse.info("Alle opplysninger mottatt, lager forslag til vedtak")

            // TODO: Shim for å gi STSB avklaringer på lik måte osm før
            val avklaringer =
                behandling.aktiveAvklaringer().map {
                    mapOf(
                        "type" to it.kode.kode,
                        "utfall" to "Manuell",
                        "begrunnelse" to it.kode.beskrivelse,
                    )
                }
            hendelse.hendelse(
                BehandlingHendelser.ForslagTilVedtakHendelse,
                "Foreslår vedtak",
                mapOf(
                    "utfall" to behandling.opplysninger.finnOpplysning(behandling.behandler.avklarer(behandling.opplysninger)).verdi,
                    "harAvklart" to
                        behandling.opplysninger
                            .finnOpplysning(behandling.behandler.avklarer(behandling.opplysninger))
                            .opplysningstype.navn,
                    "avklaringer" to avklaringer,
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

            behandling.tilstand(Ferdig(), hendelse)
        }

        override fun håndter(
            behandling: Behandling,
            hendelse: AvklaringIkkeRelevantHendelse,
        ) {
            hendelse.kontekst(this)
            if (behandling.avklaringer.avklar(hendelse.avklaringId, hendelse.kilde)) {
                hendelse.info("Avklaring er ikke lenger relevant")
                hendelse.hendelse(
                    AvklaringLukketHendelse,
                    "Avklaring ikke lenger relevant",
                    mapOf(
                        "avklaringId" to hendelse.avklaringId,
                    ),
                )
            }

            if (behandling.aktiveAvklaringer().isEmpty()) {
                if (behandling.opplysninger.finnAlle().any { it.kilde is Saksbehandlerkilde }) {
                    hendelse.info(
                        """Har ingen aktive avklaringer, men saksbehandler har lagt til opplysninger, 
                        |så vi kan ikke automatisk fatte vedtak
                        """.trimMargin(),
                    )
                    return
                }

                hendelse.info("Har ingen aktive avklaringer, går videre til vedtak")
                behandling.tilstand(Ferdig(), hendelse)
                return
            }
        }

        override fun håndter(
            behandling: Behandling,
            hendelse: NyOpplysningHendelse,
        ) {
            hendelse.kontekst(this)
            hendelse.leggTil(behandling.opplysninger)

            behandling.tilstand(Redigert(), hendelse)
        }
    }

    private data class Redigert(
        override val opprettet: LocalDateTime = LocalDateTime.now(),
    ) : BehandlingTilstand {
        override val type: TilstandType
            get() = TilstandType.Redigert

        override fun entering(
            behandling: Behandling,
            hendelse: PersonHendelse,
        ) {
            hendelse.kontekst(this)
            hendelse.info("Endret tilstand til redigert")

            // Kjør regelkjøring for alle opplysninger
            val rapport = behandling.regelkjøring.evaluer()

            rapport.kjørteRegler.forEach { regel: Regel<*> ->
                hendelse.info(regel.toString())
            }

            hendelse.lagBehov(rapport.informasjonsbehov)

            if (rapport.erFerdig()) {
                behandling.tilstand(ForslagTilVedtak(), hendelse)
            }
        }

        override fun håndter(
            behandling: Behandling,
            hendelse: NyOpplysningHendelse,
        ) {
            hendelse.kontekst(this)
            hendelse.leggTil(behandling.opplysninger)

            val rapport = behandling.regelkjøring.evaluer()

            rapport.kjørteRegler.forEach { regel: Regel<*> ->
                hendelse.info(regel.toString())
            }

            hendelse.lagBehov(rapport.informasjonsbehov)

            if (rapport.erFerdig()) {
                behandling.tilstand(ForslagTilVedtak(), hendelse)
            }
        }

        override fun håndter(
            behandling: Behandling,
            hendelse: PåminnelseHendelse,
        ) {
            hendelse.kontekst(this)
            hendelse.info("Mottak påminnelse")

            val rapport = behandling.regelkjøring.evaluer()
            if (rapport.erFerdig()) {
                hendelse.logiskFeil("Behandlingen er ferdig men vi er fortsatt i ${this.type.name}")
            }
            hendelse.lagBehov(rapport.informasjonsbehov)
        }
    }

    private data class Avbrutt(
        override val opprettet: LocalDateTime = LocalDateTime.now(),
        val årsak: String? = null,
    ) : BehandlingTilstand {
        override val type = TilstandType.Avbrutt

        override fun entering(
            behandling: Behandling,
            hendelse: PersonHendelse,
        ) {
            hendelse.info("Behandling avbrutt")
            hendelse.hendelse(
                BehandlingHendelser.AvbrytBehandlingHendelse,
                "Behandling avbrutt",
                årsak?.let { mapOf("årsak" to it) } ?: emptyMap(),
            )
            behandling.observatører.forEach { it.avbrutt() }
        }

        override fun håndter(
            behandling: Behandling,
            hendelse: AvbrytBehandlingHendelse,
        ) { // No-op
        }

        override fun håndter(
            behandling: Behandling,
            hendelse: NyOpplysningHendelse,
        ) {
            hendelse.kontekst(this)
            hendelse.info("Behandlingen er avbrutt, ignorerer opplysningssvar")
        }

        override fun håndter(
            behandling: Behandling,
            hendelse: AvklaringIkkeRelevantHendelse,
        ) {
            hendelse.kontekst(this)
            hendelse.info("Behandlingen er avbrutt, ignorerer avklaringer")
        }
    }

    private data class Ferdig(
        override val opprettet: LocalDateTime = LocalDateTime.now(),
    ) : BehandlingTilstand {
        override val type = TilstandType.Ferdig

        override fun entering(
            behandling: Behandling,
            hendelse: PersonHendelse,
        ) {
            behandling.emitFerdig()
        }

        override fun håndter(
            behandling: Behandling,
            hendelse: AvbrytBehandlingHendelse,
        ): Unit = throw IllegalStateException("Kan ikke avbryte en ferdig behandling")

        override fun håndter(
            behandling: Behandling,
            hendelse: AvklaringIkkeRelevantHendelse,
        ) {
            hendelse.kontekst(this)
            hendelse.info("Behandlingen er ferdig, ignorerer avklaringer")
        }
    }

    private fun tilstand(
        nyTilstand: BehandlingTilstand,
        hendelse: PersonHendelse,
    ) {
        if (tilstand.type == nyTilstand.type) return
        tilstand.leaving(this, hendelse)

        val forrigeTilstand = tilstand
        tilstand = nyTilstand

        hendelse.kontekst(tilstand)
        emitVedtaksperiodeEndret(forrigeTilstand)

        tilstand.entering(this, hendelse)
    }

    private fun emitFerdig() {
        val event =
            BehandlingObservatør.BehandlingFerdig(
                behandlingId = behandlingId,
                søknadId = behandler.eksternId,
                behandlingAv = behandler,
                opplysninger = opplysninger,
                automatiskBehandlet = erAutomatiskBehandlet(),
            )

        observatører.forEach { it.ferdig(event) }
    }

    private fun emitVedtaksperiodeEndret(forrigeTilstand: BehandlingTilstand) {
        val event =
            BehandlingObservatør.BehandlingEndretTilstand(
                behandlingId = behandlingId,
                gjeldendeTilstand = tilstand.type,
                forrigeTilstand = forrigeTilstand.type,
                forventetFerdig = tilstand.forventetFerdig,
                tidBrukt = Duration.between(forrigeTilstand.opprettet, tilstand.opprettet),
            )

        observatører.forEach { it.endretTilstand(event) }
    }
}

interface BehandlingObservatør {
    data class BehandlingFerdig(
        val behandlingId: UUID,
        val søknadId: EksternId<*>,
        val behandlingAv: StartHendelse,
        val opplysninger: LesbarOpplysninger,
        val automatiskBehandlet: Boolean,
    ) : PersonEvent()

    data class BehandlingEndretTilstand(
        val behandlingId: UUID,
        val gjeldendeTilstand: Behandling.TilstandType,
        val forrigeTilstand: Behandling.TilstandType,
        val forventetFerdig: LocalDateTime,
        val tidBrukt: Duration,
    ) : PersonEvent()

    fun behandlingStartet() {}

    fun forslagTilVedtak() {}

    fun avbrutt() {}

    fun ferdig(event: BehandlingFerdig) {}

    fun endretTilstand(event: BehandlingEndretTilstand) {}
}

// TODO: Vi bør ha bedre kontroll på navnene og kanskje henge sammen med behov?
@Suppress("ktlint:standard:enum-entry-name-case")
sealed class BehandlingHendelser(
    override val name: String,
) : Hendelse.Hendelsetype {
    data object BehandlingOpprettetHendelse : BehandlingHendelser("behandling_opprettet")

    data object ForslagTilVedtakHendelse : BehandlingHendelser("forslag_til_vedtak")

    data object AvklaringLukketHendelse : BehandlingHendelser("avklaring_lukket")

    data object AvbrytBehandlingHendelse : BehandlingHendelser("behandling_avbrutt")
}

private fun PersonHendelse.lagBehov(informasjonsbehov: Informasjonsbehov) =
    informasjonsbehov.onEach { (behov, avhengigheter) ->
        behov(
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
                } + this.kontekstMap(),
        )
    }
