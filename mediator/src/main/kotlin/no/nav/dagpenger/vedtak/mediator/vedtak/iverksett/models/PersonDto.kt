package no.nav.dagpenger.vedtak.mediator.vedtak.iverksett.models

import java.time.LocalDate

data class PersonDto(
    val personIdent: String? = null,
    val aktorId: String? = null,
)

data class BarnDto(
    val personIdent: String? = null,
    val termindato: LocalDate? = null,
)
