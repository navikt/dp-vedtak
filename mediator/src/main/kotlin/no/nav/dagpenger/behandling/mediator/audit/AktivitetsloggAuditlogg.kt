package no.nav.dagpenger.behandling.mediator.audit

import no.nav.dagpenger.aktivitetslogg.Aktivitetslogg
import no.nav.dagpenger.aktivitetslogg.AktivitetsloggHendelse
import no.nav.dagpenger.aktivitetslogg.AuditOperasjon
import no.nav.dagpenger.aktivitetslogg.IAktivitetslogg
import no.nav.dagpenger.aktivitetslogg.SpesifikkKontekst
import no.nav.dagpenger.behandling.mediator.AktivitetsloggMediator
import java.util.UUID

internal class AktivitetsloggAuditlogg(private val aktivitetsloggMediator: AktivitetsloggMediator) : Auditlogg {
    override fun les(
        melding: String,
        ident: String,
        saksbehandler: String,
    ) = aktivitetslogg(ident) {
        varsel(melding, ident, saksbehandler, AuditOperasjon.READ)
    }

    override fun opprett(
        melding: String,
        ident: String,
        saksbehandler: String,
    ) = aktivitetslogg(ident) {
        varsel(melding, ident, saksbehandler, AuditOperasjon.CREATE)
    }

    override fun oppdater(
        melding: String,
        ident: String,
        saksbehandler: String,
    ) = aktivitetslogg(ident) {
        varsel(melding, ident, saksbehandler, AuditOperasjon.UPDATE)
    }

    override fun slett(
        melding: String,
        ident: String,
        saksbehandler: String,
    ) = aktivitetslogg(ident) {
        varsel(melding, ident, saksbehandler, AuditOperasjon.DELETE)
    }

    private fun aktivitetslogg(
        ident: String,
        block: AuditLoggHendelse.() -> Unit,
    ) {
        val aktivitetslogg = AuditLoggHendelse(ident)
        block(aktivitetslogg)
        aktivitetsloggMediator.håndter(aktivitetslogg)
    }

    private class AuditLoggHendelse(val ident: String, private val aktivitetslogg: Aktivitetslogg = Aktivitetslogg()) :
        AktivitetsloggHendelse, IAktivitetslogg by aktivitetslogg {
        override fun ident() = ident

        override fun meldingsreferanseId() = UUID.randomUUID()

        override fun toSpesifikkKontekst() = SpesifikkKontekst("Auditlogg", mapOf("ident" to ident))
    }
}
