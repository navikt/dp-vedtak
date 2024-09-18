package no.nav.dagpenger.behandling.mediator

import no.nav.dagpenger.behandling.api.models.VedtakDTO
import no.nav.dagpenger.behandling.api.models.VedtakFastsattDTO
import no.nav.dagpenger.behandling.api.models.VedtakGjenstaaendeDTO
import no.nav.dagpenger.behandling.api.models.VilkaarDTO
import no.nav.dagpenger.behandling.modell.Behandling
import no.nav.dagpenger.behandling.modell.hendelser.SøknadInnsendtHendelse.Companion.fagsakIdOpplysningstype
import no.nav.dagpenger.opplysning.Opplysning
import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.regel.Virkningstidspunkt.virkningstidspunkt
import java.time.LocalDateTime

private val autorativKildeForDetViPåEkteMenerErVilkår: List<Opplysningstype<Boolean>> = emptyList()

private fun toVedtak(behandling: Behandling): VedtakDTO {
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
        virkningstidspunkt = opplysninger.finnOpplysning(virkningstidspunkt).verdi,
        // TODO("Vi må få med oss noe greier om saksbehandler og beslutter"),
        behandletAv = emptyList(),
        vilkaar = vilkår,
        fastsatt =
            VedtakFastsattDTO(
                utfall = false,
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
