package no.nav.dagpenger.behandling.mediator

import no.nav.dagpenger.behandling.api.models.KvoteDTO
import no.nav.dagpenger.behandling.api.models.VedtakDTO
import no.nav.dagpenger.behandling.api.models.VedtakFastsattDTO
import no.nav.dagpenger.behandling.api.models.VedtakFastsattFastsattVanligArbeidstidDTO
import no.nav.dagpenger.behandling.api.models.VedtakFastsattGrunnlagDTO
import no.nav.dagpenger.behandling.api.models.VedtakFastsattSatsDTO
import no.nav.dagpenger.behandling.api.models.VedtakGjenstEndeDTO
import no.nav.dagpenger.behandling.api.models.VilkaarDTO
import no.nav.dagpenger.behandling.mediator.api.tilOpplysningDTO
import no.nav.dagpenger.behandling.modell.Ident
import no.nav.dagpenger.behandling.modell.hendelser.SøknadInnsendtHendelse.Companion.fagsakIdOpplysningstype
import no.nav.dagpenger.behandling.modell.hendelser.SøknadInnsendtHendelse.Companion.søknadIdOpplysningstype
import no.nav.dagpenger.opplysning.LesbarOpplysninger
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
import no.nav.dagpenger.regel.TapAvArbeidsinntektOgArbeidstid.fastsattVanligArbeidstid
import no.nav.dagpenger.regel.Utdanning
import no.nav.dagpenger.regel.Utestengning
import no.nav.dagpenger.regel.fastsetting.Dagpengegrunnlag
import no.nav.dagpenger.regel.fastsetting.DagpengenesStørrelse.sats
import no.nav.dagpenger.regel.fastsetting.Dagpengeperiode
import no.nav.dagpenger.regel.fastsetting.Egenandel
import java.time.LocalDateTime
import java.util.UUID

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

fun lagVedtak(
    behandlingId: UUID,
    ident: Ident,
    opplysninger: LesbarOpplysninger,
): VedtakDTO {
    val vilkår =
        opplysninger
            .finnAlle()
            .filterIsInstance<Opplysning<Boolean>>()
            .filter { it.opplysningstype in autorativKildeForDetViPåEkteMenerErVilkår }
            .map { it.tilVilkårDTO() }

    val utfall = vilkår.all { it.status == VilkaarDTO.Status.Oppfylt }
    val fastsatt =
        when (utfall) {
            true ->
                VedtakFastsattDTO(
                    utfall = true,
                    grunnlag =
                        VedtakFastsattGrunnlagDTO(
                            opplysninger
                                .finnOpplysning(Dagpengegrunnlag.grunnlag)
                                .verdi.verdien
                                .toInt(),
                        ),
                    fastsattVanligArbeidstid =
                        VedtakFastsattFastsattVanligArbeidstidDTO(
                            opplysninger.finnOpplysning(fastsattVanligArbeidstid).verdi.toBigDecimal(),
                        ),
                    sats =
                        VedtakFastsattSatsDTO(
                            dagsatsMedBarnetillegg =
                                opplysninger
                                    .finnOpplysning(sats)
                                    .verdi.verdien
                                    .toInt(),
                        ),
                    kvoter =
                        listOf(
                            KvoteDTO(
                                "Dagpengeperiode",
                                type = KvoteDTO.Type.uker,
                                opplysninger.finnOpplysning(Dagpengeperiode.antallStønadsuker).verdi.toBigDecimal(),
                            ),
                            KvoteDTO(
                                "Egenandel",
                                type = KvoteDTO.Type.beløp,
                                opplysninger.finnOpplysning(Egenandel.egenandel).verdi.verdien,
                            ),
                        ),
                )

            false -> VedtakFastsattDTO(utfall = false)
        }

    return VedtakDTO(
        behandlingId = behandlingId,
        søknadId = opplysninger.finnOpplysning(søknadIdOpplysningstype).verdi,
        fagsakId = opplysninger.finnOpplysning(fagsakIdOpplysningstype).verdi.toString(),
        // TODO("Dette må være når vedtaket har gått til Ferdig"),
        ident = ident.identifikator(),
        vedtakstidspunkt = LocalDateTime.now(),
        // TODO: Denne må utledes igjen - virkningstidspunkt = opplysninger.finnOpplysning(virkningstidspunkt).verdi,
        virkningsdato = opplysninger.finnOpplysning(søknadstidspunkt).verdi,
        // TODO("Vi må få med oss noe greier om saksbehandler og beslutter"),
        behandletAv = emptyList(),
        vilkår = vilkår,
        fastsatt = fastsatt,
        opplysninger = opplysninger.finnAlle().map { it.tilOpplysningDTO() },
        gjenstående = VedtakGjenstEndeDTO(),
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
