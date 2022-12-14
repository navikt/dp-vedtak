package no.nav.dagpenger.vedtak.modell.hendelser

import java.time.LocalDate

class RapporteringHendelse(val meldekortDager: List<RapportertDag>)

class RapportertDag(val dato: LocalDate)
