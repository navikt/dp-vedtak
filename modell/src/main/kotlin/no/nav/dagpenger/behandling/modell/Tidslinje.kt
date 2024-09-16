package no.nav.dagpenger.behandling.modell

import java.time.LocalDateTime
import java.util.SortedSet
import java.util.UUID

class Tidslinje private constructor(
    val hendelser: SortedSet<Tidslinjehendelse>,
) : SortedSet<Tidslinjehendelse> by hendelser {
    constructor(vararg hendelser: Tidslinjehendelse) : this(hendelser.toSortedSet(sortertEtterDato))

    fun leggTilHendelse(hendelse: Tidslinjehendelse) = hendelser.add(hendelse)

    fun nesteTilBehandling(): Tidslinjehendelse? = hendelser.first { !it.erBehandlet }

    fun alle(type: Meldingstype) = hendelser.filter { it.meldingstype == type }

    fun harUbehandledeHendelser() = hendelser.any { !it.erBehandlet }

    private companion object {
        private val sortertEtterDato = Comparator.comparing(Tidslinjehendelse::opprettet)
    }
}

data class Tidslinjehendelse(
    val meldingsreferanseId: UUID,
    val meldingstype: Meldingstype,
    val opprettet: LocalDateTime,
    val behandling: Behandling? = null,
) {
    val erBehandlet = behandling != null
}

enum class Meldingstype {
    Søknad,
    Meldekort,
}
