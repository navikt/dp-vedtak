package no.nav.dagpenger.vedtak.mediator.persistens

import no.nav.dagpenger.aktivitetslogg.Aktivitetslogg
import no.nav.dagpenger.aktivitetslogg.serde.AktivitetsloggJsonBuilder

class AktivitetsloggMapper(aktivitetslogg: Aktivitetslogg) {

    private val aktiviteter = AktivitetsloggJsonBuilder(aktivitetslogg).asList()

    internal fun toMap() = mapOf(
        "aktiviteter" to aktiviteter,
    )
}
