/**
 * Familie ef sak api
 * No description provided (generated by Swagger Codegen https://github.com/swagger-api/swagger-codegen)
 *
 * OpenAPI spec version: 1.0.0
 *
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */
package no.nav.dagpenger.vedtak.mediator.vedtak.iverksett.models

/**
 *
 * @param regelId
 * @param svar
 * @param begrunnelse
 */
data class VurderingDto(

    val regelId: RegelId,
    val svar: Svar? = null,
    val begrunnelse: kotlin.String? = null,
) {
    /**
     *
     * Values: S_KERMEDLEMIFOLKETRYGDEN,MEDLEMSKAPUNNTAK,BOROGOPPHOLDERSEGINORGE,OPPHOLDUNNTAK,SKRIFTLIGAVTALEOMDELTBOSTED,N_REBOFORHOLD,MERAVDAGLIGOMSORG,OMSORGFOREGNEELLERADOPTERTEBARN,KRAVSIVILSTANDUTENP_KREVDBEGRUNNELSE,FYLLERBRUKERAKTIVITETSPLIKT,SAGTOPPELLERREDUSERT,RIMELIGGRUNNSAGTOPP,HARTIDLIGEREMOTTATTDAGPENGER,HARTIDLIGEREANDREST_NADERSOMHARBETYDNING,INNTEKTLAVEREENNINNTEKTSGRENSE,INNTEKTSAMSVARERMEDOS,ERIARBEIDELLERFORBIG_ENDESYKDOM
     */
    enum class RegelId(val value: kotlin.String) {
        S_KERMEDLEMIFOLKETRYGDEN("SØKER_MEDLEM_I_FOLKETRYGDEN"),
        MEDLEMSKAPUNNTAK("MEDLEMSKAP_UNNTAK"),
        BOROGOPPHOLDERSEGINORGE("BOR_OG_OPPHOLDER_SEG_I_NORGE"),
        OPPHOLDUNNTAK("OPPHOLD_UNNTAK"),
        SKRIFTLIGAVTALEOMDELTBOSTED("SKRIFTLIG_AVTALE_OM_DELT_BOSTED"),
        N_REBOFORHOLD("NÆRE_BOFORHOLD"),
        MERAVDAGLIGOMSORG("MER_AV_DAGLIG_OMSORG"),
        OMSORGFOREGNEELLERADOPTERTEBARN("OMSORG_FOR_EGNE_ELLER_ADOPTERTE_BARN"),
        KRAVSIVILSTANDUTENP_KREVDBEGRUNNELSE("KRAV_SIVILSTAND_UTEN_PÅKREVD_BEGRUNNELSE"),
        FYLLERBRUKERAKTIVITETSPLIKT("FYLLER_BRUKER_AKTIVITETSPLIKT"),
        SAGTOPPELLERREDUSERT("SAGT_OPP_ELLER_REDUSERT"),
        RIMELIGGRUNNSAGTOPP("RIMELIG_GRUNN_SAGT_OPP"),
        HARTIDLIGEREMOTTATTDAGPENGER("HAR_TIDLIGERE_MOTTATT_DAGPENGER"),
        HARTIDLIGEREANDREST_NADERSOMHARBETYDNING("HAR_TIDLIGERE_ANDRE_STØNADER_SOM_HAR_BETYDNING"),
        INNTEKTLAVEREENNINNTEKTSGRENSE("INNTEKT_LAVERE_ENN_INNTEKTSGRENSE"),
        INNTEKTSAMSVARERMEDOS("INNTEKT_SAMSVARER_MED_OS"),
        ERIARBEIDELLERFORBIG_ENDESYKDOM("ER_I_ARBEID_ELLER_FORBIGÅENDE_SYKDOM"),
    }

    /**
     *
     * Values: JA,NEI,MEDLEMMERENN5_RAVBRUDDMINDREENN10_R,MEDLEMMERENN7_RAVBRUDDMERENN10_R,ILANDETFORGJENFORENINGELLERGIFTESEG,ANDREFORELDERMEDLEMSISTE5_R,ANDREFORELDERMEDLEMMINST5_RAVBRUDDMINDREENN10_R,ANDREFORELDERMEDLEMMINST7_RAVBRUDDMERENN10_R,TOTALVURDERINGOPPFYLLERFORSKRIFT,MEDLEMMERENN5_RE_S,ARBEIDNORSKARBEIDSGIVER,UTENLANDSOPPHOLDMINDREENN6UKER,ERIARBEID,ETABLEREREGENVIRKSOMHET,HARFORBIG_ENDESYKDOM
     */
    enum class Svar(val value: kotlin.String) {
        JA("JA"),
        NEI("NEI"),
        MEDLEMMERENN5_RAVBRUDDMINDREENN10_R("MEDLEM_MER_ENN_5_ÅR_AVBRUDD_MINDRE_ENN_10_ÅR"),
        MEDLEMMERENN7_RAVBRUDDMERENN10_R("MEDLEM_MER_ENN_7_ÅR_AVBRUDD_MER_ENN_10ÅR"),
        ILANDETFORGJENFORENINGELLERGIFTESEG("I_LANDET_FOR_GJENFORENING_ELLER_GIFTE_SEG"),
        ANDREFORELDERMEDLEMSISTE5_R("ANDRE_FORELDER_MEDLEM_SISTE_5_ÅR"),
        ANDREFORELDERMEDLEMMINST5_RAVBRUDDMINDREENN10_R("ANDRE_FORELDER_MEDLEM_MINST_5_ÅR_AVBRUDD_MINDRE_ENN_10_ÅR"),
        ANDREFORELDERMEDLEMMINST7_RAVBRUDDMERENN10_R("ANDRE_FORELDER_MEDLEM_MINST_7_ÅR_AVBRUDD_MER_ENN_10_ÅR"),
        TOTALVURDERINGOPPFYLLERFORSKRIFT("TOTALVURDERING_OPPFYLLER_FORSKRIFT"),
        MEDLEMMERENN5_RE_S("MEDLEM_MER_ENN_5_ÅR_EØS"),
        ARBEIDNORSKARBEIDSGIVER("ARBEID_NORSK_ARBEIDSGIVER"),
        UTENLANDSOPPHOLDMINDREENN6UKER("UTENLANDSOPPHOLD_MINDRE_ENN_6_UKER"),
        ERIARBEID("ER_I_ARBEID"),
        ETABLEREREGENVIRKSOMHET("ETABLERER_EGEN_VIRKSOMHET"),
        HARFORBIG_ENDESYKDOM("HAR_FORBIGÅENDE_SYKDOM"),
    }
}
