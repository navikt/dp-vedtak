package no.nav.dagpenger.regel

import no.nav.dagpenger.behandling.konklusjon.Konklusjon

enum class DagpengerKonklusjoner(override val årsak: String) : Konklusjon {
    AvslagMinsteinntekt("Avslag på minsteinntekt"),
    AvslagAlder("Avslag på grunn av alder"),
    Innvilgelse("Personen har rett til dagpenger"),
}
