package no.nav.dagpenger.vedtak.modell.hendelser

import java.time.LocalDate

class RapporteringHendelse(val meldekortDager: List<MeldekortDag>)

class MeldekortDag(val dato: LocalDate)
