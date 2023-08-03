package no.nav.dagpenger.vedtak.iverksetting

import no.nav.dagpenger.aktivitetslogg.AktivitetsloggVisitor
import no.nav.dagpenger.vedtak.modell.PersonIdentifikator
import java.util.UUID

interface IverksettingVisitor : AktivitetsloggVisitor {

    fun visitIverksetting(id: UUID, vedtakId: UUID, personIdent: PersonIdentifikator, tilstand: Iverksetting.Tilstand) {}
}
