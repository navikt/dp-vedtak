package no.nav.dagpenger.vedtak.mediator.api

import no.nav.dagpenger.vedtak.api.models.VedtakDTO
import no.nav.dagpenger.vedtak.modell.Person
import no.nav.dagpenger.vedtak.modell.SakId
import no.nav.dagpenger.vedtak.modell.vedtak.Vedtak
import no.nav.dagpenger.vedtak.modell.visitor.PersonVisitor
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class VedtakForPersonVisitor(person: Person) : PersonVisitor {
    private val vedtakListeDto = mutableListOf<VedtakDTO>()

    init {
        person.accept(this)
    }

    fun vedtakListeDto() = vedtakListeDto.toList()

    override fun postVisitVedtak(
        vedtakId: UUID,
        sakId: SakId,
        behandlingId: UUID,
        virkningsdato: LocalDate,
        vedtakstidspunkt: LocalDateTime,
        type: Vedtak.VedtakType,
    ) {
        this.vedtakListeDto.add(
            VedtakDTO(vedtakId = vedtakId, vedtakType = VedtakDTO.VedtakType.valueOf(type.name)),
        )
    }
}
