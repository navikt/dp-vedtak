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
 * @param fagsakId
 * @param eksternId
 * @param stønadstype
 */
data class FagsakdetaljerDto(

    val fagsakId: java.util.UUID,
    val eksternId: kotlin.Long,
    val stønadstype: Stønadstype,
) {
    /**
     *
     * Values: DAGPENGER
     */
    enum class Stønadstype(val value: kotlin.String) {
        DAGPENGER("DAGPENGER"),
    }
}
