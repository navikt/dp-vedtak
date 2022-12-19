package no.nav.dagpenger.vedtak.modell.visitor

import java.time.LocalDate
import java.time.LocalDateTime

interface VedtakVisitor {

    fun visitVedtak(virkningsdato: LocalDate, beslutningstidspunkt: LocalDateTime) {}
}
