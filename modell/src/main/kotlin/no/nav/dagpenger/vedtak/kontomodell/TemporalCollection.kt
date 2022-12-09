package no.nav.dagpenger.vedtak.kontomodell

import java.time.LocalDate

internal class TemporalCollection<R> {
    private val contents = mutableMapOf<LocalDate, R>()
    private val milestones get() = contents.keys.toList().reversed()

    fun get(date: LocalDate): R = milestones
        .firstOrNull { it.isBefore(date) || it.isEqual(date) }?.let {
            contents[it]
        } ?: throw IllegalArgumentException("No records that early")

    fun put(at: LocalDate, item: R) {
        contents[at] = item
    }
}
