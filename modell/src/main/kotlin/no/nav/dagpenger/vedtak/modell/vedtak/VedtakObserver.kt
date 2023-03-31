package no.nav.dagpenger.vedtak.modell.vedtak

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

interface VedtakObserver {

    fun vedtakFattet(vedtakFattet: VedtakFattet) {}

    data class VedtakFattet(
        val vedtakId: UUID,
        val vedtakstidspunkt: LocalDateTime,
        val virkningsdato: LocalDate,
        val utfall: Utfall,

    ) {
        enum class Utfall {
            Innvilget,
            Avslått,
        }
    }
}
