package no.nav.dagpenger.behandling.modell

import no.nav.dagpenger.aktivitetslogg.Aktivitet

data class OpplysningBehov(override val name: String) : Aktivitet.Behov.Behovtype
