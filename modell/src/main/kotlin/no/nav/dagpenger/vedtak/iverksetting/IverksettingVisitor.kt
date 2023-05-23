package no.nav.dagpenger.vedtak.iverksetting

import no.nav.dagpenger.aktivitetslogg.AktivitetsloggVisitor
import java.util.UUID

interface IverksettingVisitor : AktivitetsloggVisitor {

    fun visitIverksetting(id: UUID, vedtakId: UUID, tilstand: Iverksetting.Tilstand) {}
}
