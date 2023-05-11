package no.nav.dagpenger.vedtak.iverksetting.mediator.behovløsere.models

import java.util.UUID

data class UtbetalingDto(
    val beløp: Int,
    val periode: DatoperiodeDto,
    val inntekt: Int? = null,
    val inntektsreduksjon: Int? = null,
    val samordningsfradrag: Int? = null,
    val kildeBehandlingId: UUID? = null,
)
