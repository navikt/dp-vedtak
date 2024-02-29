package no.nav.dagpenger.features.utils

import io.cucumber.java.ParameterType
import java.time.LocalDate
import java.time.format.DateTimeFormatter

internal fun String.somLocalDate(): LocalDate {
    val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    return LocalDate.parse(this, formatter)
}

@ParameterType(".*")
fun dato(dato: String): LocalDate {
    return dato.somLocalDate()
}
