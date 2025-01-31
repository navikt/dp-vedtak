package no.nav.dagpenger.behandling.mediator

import com.fasterxml.jackson.module.kotlin.convertValue
import mu.KotlinLogging
import no.nav.dagpenger.behandling.api.models.BarnDTO
import no.nav.dagpenger.behandling.api.models.BehandletAvDTO
import no.nav.dagpenger.behandling.api.models.KvoteDTO
import no.nav.dagpenger.behandling.api.models.SaksbehandlerDTO
import no.nav.dagpenger.behandling.api.models.SamordningDTO
import no.nav.dagpenger.behandling.api.models.VedtakDTO
import no.nav.dagpenger.behandling.api.models.VedtakFastsattDTO
import no.nav.dagpenger.behandling.api.models.VedtakFastsattFastsattVanligArbeidstidDTO
import no.nav.dagpenger.behandling.api.models.VedtakFastsattGrunnlagDTO
import no.nav.dagpenger.behandling.api.models.VedtakFastsattSatsDTO
import no.nav.dagpenger.behandling.api.models.VedtakGjenstEndeDTO
import no.nav.dagpenger.behandling.api.models.VilkaarDTO
import no.nav.dagpenger.behandling.mediator.api.tilOpplysningDTO
import no.nav.dagpenger.behandling.modell.Arbeidssteg
import no.nav.dagpenger.behandling.modell.Ident
import no.nav.dagpenger.behandling.modell.hendelser.EksternId
import no.nav.dagpenger.behandling.objectMapper
import no.nav.dagpenger.opplysning.LesbarOpplysninger
import no.nav.dagpenger.opplysning.Opplysning
import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.opplysning.verdier.Beløp
import no.nav.dagpenger.regel.Alderskrav
import no.nav.dagpenger.regel.FulleYtelser
import no.nav.dagpenger.regel.KravPåDagpenger.kravPåDagpenger
import no.nav.dagpenger.regel.KravPåDagpenger.minsteinntektEllerVerneplikt
import no.nav.dagpenger.regel.Minsteinntekt.minsteinntekt
import no.nav.dagpenger.regel.Opphold
import no.nav.dagpenger.regel.ReellArbeidssøker
import no.nav.dagpenger.regel.ReellArbeidssøker.oppyllerKravTilRegistrertArbeidssøker
import no.nav.dagpenger.regel.Rettighetstype
import no.nav.dagpenger.regel.SamordingUtenforFolketrygden
import no.nav.dagpenger.regel.Samordning
import no.nav.dagpenger.regel.StreikOgLockout
import no.nav.dagpenger.regel.SøknadInnsendtHendelse.Companion.fagsakIdOpplysningstype
import no.nav.dagpenger.regel.Søknadstidspunkt.prøvingsdato
import no.nav.dagpenger.regel.TapAvArbeidsinntektOgArbeidstid
import no.nav.dagpenger.regel.TapAvArbeidsinntektOgArbeidstid.nyArbeidstid
import no.nav.dagpenger.regel.Utdanning
import no.nav.dagpenger.regel.Utestengning
import no.nav.dagpenger.regel.fastsetting.Dagpengegrunnlag
import no.nav.dagpenger.regel.fastsetting.DagpengenesStørrelse.barn
import no.nav.dagpenger.regel.fastsetting.DagpengenesStørrelse.dagsatsEtterSamordningMedBarnetillegg
import no.nav.dagpenger.regel.fastsetting.Dagpengeperiode
import no.nav.dagpenger.regel.fastsetting.Egenandel
import no.nav.dagpenger.regel.fastsetting.Vanligarbeidstid.fastsattVanligArbeidstid
import no.nav.dagpenger.regel.fastsetting.VernepliktFastsetting.grunnlagForVernepliktErGunstigst
import no.nav.dagpenger.regel.fastsetting.VernepliktFastsetting.vernepliktPeriode
import java.lang.ProcessBuilder.Redirect.to
import java.time.LocalDateTime
import java.util.UUID

private fun folketrygdloven(paragraf: String) = "folketrygdloven § $paragraf"

private fun kapittel4(paragraf: Int) = folketrygdloven("4-$paragraf")

private val autorativKildeForDetViPåEkteMenerErVilkår: Map<Opplysningstype<Boolean>, String> =
    mapOf(
        Alderskrav.kravTilAlder to kapittel4(23),
        FulleYtelser.ikkeFulleYtelser to kapittel4(24),
        Opphold.oppfyllerMedlemskap to kapittel4(2),
        oppyllerKravTilRegistrertArbeidssøker to kapittel4(5),
        minsteinntektEllerVerneplikt to kapittel4(4),
        Opphold.oppfyllerKravet to kapittel4(5),
        ReellArbeidssøker.kravTilArbeidssøker to kapittel4(5),
        ReellArbeidssøker.oppfyllerKravTilArbeidsfør to kapittel4(5),
        ReellArbeidssøker.oppfyllerKravTilArbeidssøker to kapittel4(5),
        ReellArbeidssøker.oppfyllerKravTilMobilitet to kapittel4(5),
        ReellArbeidssøker.oppfyllerKravetTilEthvertArbeid to kapittel4(5),
        Rettighetstype.rettighetstype to "folketrygdloven kapittel 4",
        StreikOgLockout.ikkeStreikEllerLockout to kapittel4(22),
        TapAvArbeidsinntektOgArbeidstid.kravTilTapAvArbeidsinntekt to kapittel4(3),
        TapAvArbeidsinntektOgArbeidstid.kravTilTaptArbeidstid to kapittel4(3),
        TapAvArbeidsinntektOgArbeidstid.kravTilTapAvArbeidsinntektOgArbeidstid to kapittel4(3),
        Utdanning.kravTilUtdanning to kapittel4(6),
        Utestengning.oppfyllerKravetTilIkkeUtestengt to kapittel4(28),
    )

private fun LesbarOpplysninger.samordninger(): List<SamordningDTO> {
    @Suppress("UNCHECKED_CAST")
    val ytelser: List<Opplysning<Beløp>> =
        (
            finnAlle(
                listOf(
                    Samordning.sykepengerDagsats,
                    Samordning.pleiepengerDagsats,
                    Samordning.omsorgspengerDagsats,
                    Samordning.opplæringspengerDagsats,
                    Samordning.uføreDagsats,
                    Samordning.foreldrepengerDagsats,
                    Samordning.svangerskapspengerDagsats,
                    SamordingUtenforFolketrygden.pensjonFraOffentligTjenestepensjonsordningBeløp,
                    SamordingUtenforFolketrygden.redusertUførepensjonBeløp,
                    SamordingUtenforFolketrygden.vartpengerBeløp,
                    SamordingUtenforFolketrygden.ventelønnBeløp,
                    SamordingUtenforFolketrygden.etterlønnBeløp,
                    SamordingUtenforFolketrygden.garantilottGFFBeløp,
                ),
            ) as List<Opplysning<Beløp>>
        ).filterNot {
            it.verdi == Beløp(0.0)
        }

    return ytelser.map {
        SamordningDTO(
            type = it.opplysningstype.navn,
            beløp = it.verdi.verdien,
            // TODO: Vi må mappe til riktig grad
            grad = 50.toBigDecimal(),
        )
    }
}

private val logger = KotlinLogging.logger { }

fun lagVedtak(
    behandlingId: UUID,
    ident: Ident,
    søknadId: EksternId<*>,
    opplysninger: LesbarOpplysninger,
    automatisk: Boolean,
    godkjentAv: Arbeidssteg,
    besluttetAv: Arbeidssteg,
): VedtakDTO {
    val vilkår =
        opplysninger
            .finnAlle()
            .filterIsInstance<Opplysning<Boolean>>()
            .filter { it.opplysningstype in autorativKildeForDetViPåEkteMenerErVilkår.keys }
            .map { it.tilVilkårDTO(autorativKildeForDetViPåEkteMenerErVilkår[it.opplysningstype]) }

    val utfall = vilkår.all { it.status == VilkaarDTO.Status.Oppfylt } && opplysninger.erSann(kravPåDagpenger)
    logger.info {
        "VedtakDTO med utfall $utfall, dette var alle vilkårene ${vilkår.joinToString("\n") { it.navn + " -> " + it.status.name }}"
    }
    val fastsatt = vedtakFastsattDTO(utfall, opplysninger)

    return VedtakDTO(
        behandlingId = behandlingId,
        søknadId = søknadId.id.toString(),
        fagsakId = opplysninger.finnOpplysning(fagsakIdOpplysningstype).verdi.toString(),
        automatisk = automatisk,
        ident = ident.identifikator(),
        vedtakstidspunkt = LocalDateTime.now(),
        // TODO: Denne må utledes igjen - virkningstidspunkt = opplysninger.finnOpplysning(virkningstidspunkt).verdi,
        virkningsdato = opplysninger.finnOpplysning(prøvingsdato).verdi,
        behandletAv =
            listOfNotNull(
                godkjentAv.takeIf { it.erUtført }?.let {
                    BehandletAvDTO(
                        BehandletAvDTO.Rolle.saksbehandler,
                        SaksbehandlerDTO(it.utførtAv.ident),
                    )
                },
                besluttetAv.takeIf { it.erUtført }?.let {
                    BehandletAvDTO(
                        BehandletAvDTO.Rolle.beslutter,
                        SaksbehandlerDTO(it.utførtAv.ident),
                    )
                },
            ),
        vilkår = vilkår,
        fastsatt = fastsatt,
        gjenstående = VedtakGjenstEndeDTO(),
        utbetalinger = emptyList(),
        opplysninger = opplysninger.finnAlle().map { it.tilOpplysningDTO(opplysninger) },
    )
}

private fun vedtakFastsattDTO(
    utfall: Boolean,
    opplysninger: LesbarOpplysninger,
) = when (utfall) {
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
                    vanligArbeidstidPerUke = opplysninger.finnOpplysning(fastsattVanligArbeidstid).verdi.toBigDecimal(),
                    nyArbeidstidPerUke = opplysninger.finnOpplysning(nyArbeidstid).verdi.toBigDecimal(),
                ),
            sats =
                VedtakFastsattSatsDTO(
                    dagsatsMedBarnetillegg =
                        opplysninger
                            .finnOpplysning(dagsatsEtterSamordningMedBarnetillegg)
                            .verdi.verdien
                            .toInt(),
                    barn =
                        opplysninger.finnOpplysning(barn).verdi.map {
                            BarnDTO(it.fødselsdato, it.kvalifiserer)
                        },
                ),
            samordning = opplysninger.samordninger(),
            kvoter =
                listOfNotNull(
                    opplysninger.finnOpplysning(minsteinntekt).takeIf { it.verdi }?.let {
                        KvoteDTO(
                            "Dagpengeperiode",
                            KvoteDTO.Type.uker,
                            opplysninger.finnOpplysning(Dagpengeperiode.ordinærPeriode).verdi.toBigDecimal(),
                        )
                    },
                    runCatching { opplysninger.finnOpplysning(grunnlagForVernepliktErGunstigst) }
                        .getOrNull()
                        .takeIf { it?.verdi == true }
                        ?.let {
                            KvoteDTO(
                                "Verneplikt",
                                KvoteDTO.Type.uker,
                                opplysninger.finnOpplysning(vernepliktPeriode).verdi.toBigDecimal(),
                            )
                        },
                    KvoteDTO(
                        "Egenandel",
                        KvoteDTO.Type.beløp,
                        opplysninger.finnOpplysning(Egenandel.egenandel).verdi.verdien,
                    ),
                ),
        )

    false ->
        VedtakFastsattDTO(
            utfall = false,
            fastsattVanligArbeidstid =
                opplysninger.har(kravPåDagpenger).takeIf { it }?.let {
                    VedtakFastsattFastsattVanligArbeidstidDTO(
                        vanligArbeidstidPerUke = opplysninger.finnOpplysning(fastsattVanligArbeidstid).verdi.toBigDecimal(),
                        nyArbeidstidPerUke = opplysninger.finnOpplysning(nyArbeidstid).verdi.toBigDecimal(),
                    )
                },
            samordning = emptyList(),
        )
}

private fun Opplysning<Boolean>.tilVilkårDTO(hjemmel: String?): VilkaarDTO =
    VilkaarDTO(
        navn = this.opplysningstype.toString(),
        hjemmel = hjemmel ?: "Mangler mapping til hjemmel",
        status =
            when (this.verdi) {
                true -> VilkaarDTO.Status.Oppfylt
                else -> VilkaarDTO.Status.IkkeOppfylt
            },
        vurderingstidspunkt = this.opprettet,
    )

fun VedtakDTO.toMap() = objectMapper.convertValue<Map<String, Any>>(this)
