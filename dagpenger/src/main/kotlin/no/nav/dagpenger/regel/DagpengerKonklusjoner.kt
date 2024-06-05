package no.nav.dagpenger.regel

import no.nav.dagpenger.behandling.konklusjon.Konklusjon

enum class DagpengerKonklusjoner(override val årsak: String) : Konklusjon {
    Minsteinntekt("Avslag på minsteinntekt"),
    Alder("Avslag på grunn av alder"),
    Innvilgelse("Personen har rett til dagpenger"),
}
