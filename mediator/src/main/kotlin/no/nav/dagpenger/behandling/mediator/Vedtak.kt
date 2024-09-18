package no.nav.dagpenger.behandling.mediator

import no.nav.dagpenger.behandling.api.models.VedtakDTO
import no.nav.dagpenger.behandling.api.models.VedtakFastsattDTO
import no.nav.dagpenger.behandling.api.models.VedtakGjenstaaendeDTO
import no.nav.dagpenger.behandling.api.models.VilkaarDTO
import no.nav.dagpenger.behandling.modell.Behandling
import no.nav.dagpenger.behandling.modell.hendelser.SøknadInnsendtHendelse.Companion.fagsakIdOpplysningstype
import no.nav.dagpenger.opplysning.Opplysning
import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.regel.Alderskrav
import no.nav.dagpenger.regel.Medlemskap
import no.nav.dagpenger.regel.Meldeplikt
import no.nav.dagpenger.regel.Minsteinntekt
import no.nav.dagpenger.regel.ReellArbeidssøker
import no.nav.dagpenger.regel.Rettighetstype
import no.nav.dagpenger.regel.StreikOgLockout
import no.nav.dagpenger.regel.Søknadstidspunkt.søknadstidspunkt
import no.nav.dagpenger.regel.TapAvArbeidsinntektOgArbeidstid
import no.nav.dagpenger.regel.Utdanning
import no.nav.dagpenger.regel.Utestengning
import java.time.LocalDateTime

private val autorativKildeForDetViPåEkteMenerErVilkår: List<Opplysningstype<Boolean>> =
    listOf(
        Alderskrav.kravTilAlder,
        Minsteinntekt.minsteinntekt,
        ReellArbeidssøker.kravTilArbeidssøker,
        Meldeplikt.registrertPåSøknadstidspunktet,
        Rettighetstype.rettighetstype,
        Utdanning.kravTilUtdanning,
        Utestengning.ikkeUtestengt,
        StreikOgLockout.ikkeStreikEllerLockout,
        Medlemskap.oppfyllerMedlemskap,
        TapAvArbeidsinntektOgArbeidstid.kravTilTapAvArbeidsinntektOgArbeidstid,
    )

fun lagVedtak(behandling: Behandling): VedtakDTO {
    val opplysninger = behandling.opplysninger()
    val vilkår =
        opplysninger
            .finnAlle()
            .filterIsInstance<Opplysning<Boolean>>()
            .filter { it.opplysningstype in autorativKildeForDetViPåEkteMenerErVilkår }
            .map { it.tilVilkårDTO() }

    return VedtakDTO(
        behandlingId = behandling.behandlingId,
        fagsakId = opplysninger.finnOpplysning(fagsakIdOpplysningstype).verdi.toString(),
        // TODO("Dette må være når vedtaket har gått til Ferdig"),
        vedtakstidspunkt = LocalDateTime.now(),
        // TODO: Denne må utledes igjen - virkningstidspunkt = opplysninger.finnOpplysning(virkningstidspunkt).verdi,
        virkningstidspunkt = opplysninger.finnOpplysning(søknadstidspunkt).verdi,
        // TODO("Vi må få med oss noe greier om saksbehandler og beslutter"),
        behandletAv = emptyList(),
        vilkaar = vilkår,
        fastsatt =
            VedtakFastsattDTO(
                utfall = vilkår.all { it.status == VilkaarDTO.Status.Oppfylt },
            ),
        gjenstaaende = VedtakGjenstaaendeDTO(),
        utbetalinger = emptyList(),
    )
}

private fun Opplysning<Boolean>.tilVilkårDTO(): VilkaarDTO =
    VilkaarDTO(
        navn = this.opplysningstype.toString(),
        hjemmel = "Lover og sånt",
        status =
            when (this.verdi) {
                true -> VilkaarDTO.Status.Oppfylt
                else -> VilkaarDTO.Status.IkkeOppfylt
            },
        vurderingstidspunkt = this.opprettet,
    )
