package no.nav.dagpenger.behandling.modell

import no.nav.dagpenger.aktivitetslogg.Aktivitetskontekst
import no.nav.dagpenger.aktivitetslogg.SpesifikkKontekst
import no.nav.dagpenger.aktivitetslogg.aktivitet.Hendelse
import no.nav.dagpenger.avklaring.Avklaring
import no.nav.dagpenger.avklaring.Avklaringer
import no.nav.dagpenger.behandling.modell.Behandling.BehandlingTilstand.Companion.fraType
import no.nav.dagpenger.behandling.modell.BehandlingHendelser.AvklaringLukketHendelse
import no.nav.dagpenger.behandling.modell.PersonObservatør.PersonEvent
import no.nav.dagpenger.behandling.modell.hendelser.AvbrytBehandlingHendelse
import no.nav.dagpenger.behandling.modell.hendelser.AvklaringIkkeRelevantHendelse
import no.nav.dagpenger.behandling.modell.hendelser.AvklaringKvittertHendelse
import no.nav.dagpenger.behandling.modell.hendelser.BesluttBehandlingHendelse
import no.nav.dagpenger.behandling.modell.hendelser.EksternId
import no.nav.dagpenger.behandling.modell.hendelser.ForslagGodkjentHendelse
import no.nav.dagpenger.behandling.modell.hendelser.GodkjennBehandlingHendelse
import no.nav.dagpenger.behandling.modell.hendelser.LåsHendelse
import no.nav.dagpenger.behandling.modell.hendelser.LåsOppHendelse
import no.nav.dagpenger.behandling.modell.hendelser.OpplysningSvarHendelse
import no.nav.dagpenger.behandling.modell.hendelser.PersonHendelse
import no.nav.dagpenger.behandling.modell.hendelser.PåminnelseHendelse
import no.nav.dagpenger.behandling.modell.hendelser.SendTilbakeHendelse
import no.nav.dagpenger.behandling.modell.hendelser.StartHendelse
import no.nav.dagpenger.opplysning.Hypotese
import no.nav.dagpenger.opplysning.Informasjonsbehov
import no.nav.dagpenger.opplysning.LesbarOpplysninger
import no.nav.dagpenger.opplysning.Opplysning
import no.nav.dagpenger.opplysning.Opplysninger
import no.nav.dagpenger.opplysning.Regelkjøring
import no.nav.dagpenger.opplysning.Saksbehandler
import no.nav.dagpenger.opplysning.regel.Regel
import no.nav.dagpenger.opplysning.verdier.Ulid
import no.nav.dagpenger.uuid.UUIDv7
import java.time.Duration
import java.time.LocalDateTime
import java.util.UUID

data class Arbeidssteg(
    val utførtAv: Saksbehandler,
    val utført: LocalDateTime = LocalDateTime.now(),
)

enum class Oppgave {
    Godkjent,
    Besluttet,
}

class Behandling private constructor(
    val behandlingId: UUID,
    val behandler: StartHendelse,
    gjeldendeOpplysninger: Opplysninger,
    val basertPå: List<Behandling> = emptyList(),
    private var _godkjent: Arbeidssteg? = null,
    private var _besluttet: Arbeidssteg? = null,
    private var tilstand: BehandlingTilstand,
    avklaringer: List<Avklaring>,
) : Aktivitetskontekst,
    BehandlingHåndter {
    constructor(
        behandler: StartHendelse,
        opplysninger: List<Opplysning<*>>,
        basertPå: List<Behandling> = emptyList(),
    ) : this(
        behandlingId = UUIDv7.ny(),
        behandler = behandler,
        gjeldendeOpplysninger = Opplysninger(opplysninger),
        basertPå = basertPå,
        tilstand = UnderOpprettelse(LocalDateTime.now()),
        avklaringer = emptyList(),
    )

    val godkjent get() = _godkjent
    val besluttet get() = _besluttet

    init {
        require(basertPå.all { it.tilstand is Ferdig }) {
            "Kan ikke basere en ny behandling på en som ikke er ferdig"
        }
    }

    private val observatører = mutableListOf<BehandlingObservatør>()

    private val tidligereOpplysninger: List<Opplysninger> = basertPå.map { it.opplysninger }

    private val opplysninger: Opplysninger =
        (gjeldendeOpplysninger + tidligereOpplysninger)

    private val regelkjøring: Regelkjøring get() = behandler.regelkjøring(opplysninger)

    private val kontrollpunkter =
        when (tilstand) {
            is Avbrutt -> emptyList()
            is Ferdig -> emptyList()
            else -> behandler.kontrollpunkter()
        }
    private val avklaringer = Avklaringer(kontrollpunkter, avklaringer)

    fun avklaringer() = avklaringer.avklaringer(opplysninger.forDato(behandler.prøvingsdato(opplysninger)))

    fun erAutomatiskBehandlet() = avklaringer().all { it.erAvklart() || it.erAvbrutt() }

    fun aktiveAvklaringer() = avklaringer.måAvklares(opplysninger.forDato(behandler.prøvingsdato(opplysninger)))

    fun kreverTotrinnskontroll() = behandler.kreverTotrinnskontroll(opplysninger)

    companion object {
        fun rehydrer(
            behandlingId: UUID,
            behandler: StartHendelse,
            gjeldendeOpplysninger: Opplysninger,
            basertPå: List<Behandling> = emptyList(),
            tilstand: TilstandType,
            sistEndretTilstand: LocalDateTime,
            avklaringer: List<Avklaring>,
            godkjent: Arbeidssteg? = null,
            besluttet: Arbeidssteg? = null,
        ) = Behandling(
            behandlingId = behandlingId,
            behandler = behandler,
            gjeldendeOpplysninger = gjeldendeOpplysninger,
            basertPå = basertPå,
            tilstand = fraType(tilstand, sistEndretTilstand),
            avklaringer = avklaringer,
            _godkjent = godkjent,
            _besluttet = besluttet,
        )

        fun List<Behandling>.finn(behandlingId: UUID) =
            try {
                single { it.behandlingId == behandlingId }
            } catch (e: IllegalArgumentException) {
                throw IllegalArgumentException("Fant flere behandlinger med samme id, id=$behandlingId", e)
            }
    }

    fun tilstand() = Pair(tilstand.type, tilstand.opprettet)

    fun harTilstand(tilstand: TilstandType) = this.tilstand.type == tilstand

    fun opplysninger(): LesbarOpplysninger = opplysninger

    override fun håndter(hendelse: StartHendelse) {
        hendelse.kontekst(this)
        tilstand.håndter(this, hendelse)
    }

    override fun håndter(hendelse: AvklaringIkkeRelevantHendelse) {
        hendelse.kontekst(this)
        tilstand.håndter(this, hendelse)
    }

    override fun håndter(hendelse: AvklaringKvittertHendelse) {
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

    override fun håndter(hendelse: LåsHendelse) {
        hendelse.kontekst(this)
        tilstand.håndter(this, hendelse)
    }

    override fun håndter(hendelse: LåsOppHendelse) {
        hendelse.kontekst(this)
        tilstand.håndter(this, hendelse)
    }

    override fun håndter(hendelse: PåminnelseHendelse) {
        hendelse.kontekst(this)
        tilstand.håndter(this, hendelse)
    }

    override fun håndter(hendelse: GodkjennBehandlingHendelse) {
        hendelse.kontekst(this)
        tilstand.håndter(this, hendelse)
    }

    override fun håndter(hendelse: BesluttBehandlingHendelse) {
        hendelse.kontekst(this)
        tilstand.håndter(this, hendelse)
    }

    override fun håndter(hendelse: SendTilbakeHendelse) {
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
        Låst,
        Avbrutt,
        Ferdig,
        Redigert,
        Godkjenning,
        Kontroll,
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
                TilstandType.Låst -> Låst(opprettet)
                TilstandType.Avbrutt -> Avbrutt(opprettet)
                TilstandType.Ferdig -> Ferdig(opprettet)
                TilstandType.Redigert -> Redigert(opprettet)
                TilstandType.Godkjenning -> Godkjenning(opprettet)
                TilstandType.Kontroll -> Kontroll(opprettet)
            }
        }

        fun entering(
            behandling: Behandling,
            hendelse: PersonHendelse,
        ) {
        }

        fun håndter(
            behandling: Behandling,
            hendelse: StartHendelse,
        ): Unit =
            throw IllegalStateException(
                "Kan ikke håndtere hendelse ${hendelse.javaClass.simpleName} i tilstand ${this.javaClass.simpleName}",
            )

        fun håndter(
            behandling: Behandling,
            hendelse: OpplysningSvarHendelse,
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
            hendelse: LåsHendelse,
        ): Unit =
            throw IllegalStateException(
                "Kan ikke håndtere hendelse ${hendelse.javaClass.simpleName} i tilstand ${this.javaClass.simpleName}",
            )

        fun håndter(
            behandling: Behandling,
            hendelse: LåsOppHendelse,
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
            hendelse: AvklaringKvittertHendelse,
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

        fun håndter(
            behandling: Behandling,
            hendelse: GodkjennBehandlingHendelse,
        ) {
            hendelse.info("Behandlingen skal godkjennes, men tilstanden støtter ikke dette")
        }

        fun håndter(
            behandling: Behandling,
            hendelse: BesluttBehandlingHendelse,
        ) {
            hendelse.info("Behandlingen skal besluttes, men tilstanden støtter ikke dette")
        }

        fun håndter(
            behandling: Behandling,
            hendelse: SendTilbakeHendelse,
        ) {
            hendelse.info("Behandlingen skal sendest tilbake fra totrinnskontroll, men tilstanden støtter ikke dette")
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
            hendelse: StartHendelse,
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
            val rapport = behandling.regelkjøring.evaluer()

            rapport.kjørteRegler.forEach { regel: Regel<*> ->
                hendelse.info(regel.toString())
            }

            hendelse.lagBehov(rapport.informasjonsbehov)

            if (rapport.erFerdig()) {
                behandling.brut001(hendelse)
            }
        }

        override fun håndter(
            behandling: Behandling,
            hendelse: PåminnelseHendelse,
        ) {
            hendelse.kontekst(this)
            hendelse.info("Mottok påminnelse om at behandlingen står fast")
            val rapport = behandling.regelkjøring.evaluer()
            if (rapport.erFerdig()) {
                hendelse.logiskFeil("Behandlingen er ferdig men vi er fortsatt i ${this.type.name}")
            }
            hendelse.lagBehov(rapport.informasjonsbehov)
        }

        override fun håndter(
            behandling: Behandling,
            hendelse: OpplysningSvarHendelse,
        ) {
            hendelse.kontekst(this)
            hendelse.opplysninger.forEach { opplysning ->
                hendelse.info("Mottok svar på opplysning om ${opplysning.opplysningstype}")
                opplysning.leggTil(behandling.opplysninger)
            }

            // Kjør regelkjøring for alle opplysninger
            val rapport = behandling.regelkjøring.evaluer()
            rapport.kjørteRegler.forEach { regel: Regel<*> ->
                hendelse.info(regel.toString())
            }

            if (!behandling.behandler.støtterInnvilgelse(behandling.opplysninger)) {
                // TODO: Dette faller bort når vi sjekker alt
                val kravPåDagpenger =
                    behandling.behandler.kravPåDagpenger(behandling.opplysninger)
                if (kravPåDagpenger) {
                    hendelse.info("Behandling fører ikke til avslag, det støtter vi ikke enda")
                    behandling.tilstand(Avbrutt(årsak = "Førte ikke til avslag"), hendelse)
                    return
                }

                // TODO: Dette faller bort når vi sjekker alt
                val kravTilInntekt =
                    behandling.behandler.minsteinntekt(behandling.opplysninger)
                if (kravTilInntekt) {
                    hendelse.info("Behandling er avslag, men kravet til inntekt er oppfylt, det støtter vi ikke enda")
                    behandling.tilstand(Avbrutt(årsak = "Førte ikke til avslag på grunn av inntekt"), hendelse)
                    return
                }
            }

            hendelse.lagBehov(rapport.informasjonsbehov)

            if (rapport.erFerdig()) {
                behandling.brut001(hendelse)
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
            val prøvingsdato = behandling.behandler.prøvingsdato(behandling.opplysninger)
            val avklarer = behandling.behandler.avklarer(behandling.opplysninger)
            hendelse.hendelse(
                BehandlingHendelser.ForslagTilVedtakHendelse,
                "Foreslår vedtak",
                mapOf(
                    "prøvingsdato" to prøvingsdato,
                    "utfall" to behandling.opplysninger.finnOpplysning(avklarer).verdi,
                    "harAvklart" to
                        behandling.opplysninger
                            .finnOpplysning(avklarer)
                            .opplysningstype.navn,
                    "avklaringer" to avklaringer,
                ),
            )
            behandling.observatører.forEach { it.forslagTilVedtak() }
        }

        override fun håndter(
            behandling: Behandling,
            hendelse: LåsHendelse,
        ) {
            hendelse.kontekst(this)
            hendelse.info("Behandling sendt til kontroll")

            behandling.tilstand(Låst(), hendelse)
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

            behandling.brut001(hendelse)
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

            behandling.brut001(hendelse)
        }

        override fun håndter(
            behandling: Behandling,
            hendelse: AvklaringKvittertHendelse,
        ) {
            hendelse.kontekst(this)

            behandling.avklaringer.kvitter(hendelse.avklaringId, hendelse.kilde, hendelse.begrunnelse)
            hendelse.info("Avklaring er kvittert")
        }

        override fun håndter(
            behandling: Behandling,
            hendelse: OpplysningSvarHendelse,
        ) {
            hendelse.kontekst(this)
            hendelse.info("Fikk svar på opplysning i ${this.type.name}.")

            hendelse.opplysninger.forEach { opplysning ->
                hendelse.info("Mottok svar på opplysning om ${opplysning.opplysningstype}")
                opplysning.leggTil(behandling.opplysninger)
            }

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
                behandling.brut001(hendelse)
            }
        }

        override fun håndter(
            behandling: Behandling,
            hendelse: OpplysningSvarHendelse,
        ) {
            hendelse.opplysninger.forEach { opplysning ->
                hendelse.info("Mottok svar på opplysning om ${opplysning.opplysningstype}")
                opplysning.leggTil(behandling.opplysninger)
            }

            val rapport = behandling.regelkjøring.evaluer()

            rapport.kjørteRegler.forEach { regel: Regel<*> ->
                hendelse.info(regel.toString())
            }

            hendelse.info(
                "Fant ${rapport.foreldreløse.size} foreldreløse regler=${rapport.foreldreløse.joinToString("\n") { it.toString() }}",
            )

            hendelse.lagBehov(rapport.informasjonsbehov)

            if (rapport.erFerdig()) {
                behandling.brut001(hendelse)
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
            hendelse: OpplysningSvarHendelse,
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

    private data class Låst(
        override val opprettet: LocalDateTime = LocalDateTime.now(),
    ) : BehandlingTilstand {
        override val type = TilstandType.Låst

        override fun håndter(
            behandling: Behandling,
            hendelse: LåsOppHendelse,
        ) {
            hendelse.kontekst(this)
            hendelse.info("Behandlingen ble ikke godkjent, settes tilbake til forslag")

            behandling.tilstand(ForslagTilVedtak(), hendelse)
        }

        override fun håndter(
            behandling: Behandling,
            hendelse: ForslagGodkjentHendelse,
        ) {
            hendelse.kontekst(this)
            hendelse.info("Forslag til vedtak godkjent")

            behandling.brut001(hendelse)
        }

        override fun håndter(
            behandling: Behandling,
            hendelse: AvbrytBehandlingHendelse,
        ): Unit = throw IllegalStateException("Kan ikke avbryte en låst behandling")

        override fun håndter(
            behandling: Behandling,
            hendelse: AvklaringIkkeRelevantHendelse,
        ) {
            hendelse.kontekst(this)
            hendelse.info("Behandlingen er låst, ignorerer avklaringer")
        }

        override fun håndter(
            behandling: Behandling,
            hendelse: OpplysningSvarHendelse,
        ) {
            hendelse.kontekst(this)
            hendelse.info("Behandlingen er låst, ignorerer opplysningssvar")
        }

        override fun håndter(
            behandling: Behandling,
            hendelse: LåsHendelse,
        ) {
            hendelse.kontekst(this)
            hendelse.info("Behandlingen er allerede låst")
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

        override fun håndter(
            behandling: Behandling,
            hendelse: OpplysningSvarHendelse,
        ) {
            hendelse.kontekst(this)
            hendelse.info("Behandlingen er ferdig, ignorerer opplysningssvar")
        }
    }

    private class Godkjenning(
        override val opprettet: LocalDateTime = LocalDateTime.now(),
    ) : BehandlingTilstand {
        override val type = TilstandType.Godkjenning

        override fun håndter(
            behandling: Behandling,
            hendelse: GodkjennBehandlingHendelse,
        ) {
            behandling._godkjent = Arbeidssteg(hendelse.godkjentAv)
            behandling.tilstand(Kontroll(), hendelse)
        }

        override fun håndter(
            behandling: Behandling,
            hendelse: OpplysningSvarHendelse,
        ) {
            hendelse.kontekst(this)
            hendelse.info("Fikk svar på opplysning i ${this.type.name}.")

            hendelse.opplysninger.forEach { opplysning ->
                hendelse.info("Mottok svar på opplysning om ${opplysning.opplysningstype}")
                opplysning.leggTil(behandling.opplysninger)
            }

            behandling.tilstand(Redigert(), hendelse)
        }

        override fun håndter(
            behandling: Behandling,
            hendelse: AvbrytBehandlingHendelse,
        ): Unit = throw IllegalStateException("Kan ikke avbryte en låst behandling")

        override fun håndter(
            behandling: Behandling,
            hendelse: AvklaringIkkeRelevantHendelse,
        ) {
            hendelse.kontekst(this)
            hendelse.info("Behandlingen er låst, ignorerer avklaringer")
        }
    }

    private class Kontroll(
        override val opprettet: LocalDateTime = LocalDateTime.now(),
    ) : BehandlingTilstand {
        override val type = TilstandType.Kontroll

        override fun håndter(
            behandling: Behandling,
            hendelse: BesluttBehandlingHendelse,
        ) {
            if (behandling.godkjent?.utførtAv == hendelse.besluttetAv) {
                throw IllegalArgumentException("Beslutter kan ikke være samme som saksbehandler")
            }

            behandling._besluttet = Arbeidssteg(hendelse.besluttetAv)
            behandling.tilstand(Ferdig(), hendelse)
        }

        override fun håndter(
            behandling: Behandling,
            hendelse: SendTilbakeHendelse,
        ) {
            behandling._godkjent = null
            behandling.tilstand(Godkjenning(), hendelse)
        }

        override fun håndter(
            behandling: Behandling,
            hendelse: AvbrytBehandlingHendelse,
        ): Unit = throw IllegalStateException("Kan ikke avbryte en låst behandling")

        override fun håndter(
            behandling: Behandling,
            hendelse: AvklaringIkkeRelevantHendelse,
        ) {
            hendelse.kontekst(this)
            hendelse.info("Behandlingen er låst, ignorerer avklaringer")
        }

        override fun håndter(
            behandling: Behandling,
            hendelse: OpplysningSvarHendelse,
        ) {
            hendelse.kontekst(this)
            hendelse.info("Behandlingen er låst, ignorerer opplysningssvar")
        }
    }

    // Behandlingen er ferdig og vi må rute til enten ferdig, forslag, eller godkjenning
    // TODO: Lag et vakrere navn
    private fun brut001(hendelse: PersonHendelse) {
        if (this.aktiveAvklaringer().isNotEmpty()) {
            return tilstand(ForslagTilVedtak(), hendelse)
        }

        if (!behandler.kreverTotrinnskontroll(opplysninger)) {
            return tilstand(Ferdig(), hendelse)
        }

        return tilstand(Godkjenning(), hendelse)
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
                godkjent = godkjent,
                besluttet = besluttet,
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
        val godkjent: Arbeidssteg? = null,
        val besluttet: Arbeidssteg? = null,
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
                } + this.kontekstMap() + mapOf("@utledetAv" to avhengigheter.map { it.id }),
        )
    }
