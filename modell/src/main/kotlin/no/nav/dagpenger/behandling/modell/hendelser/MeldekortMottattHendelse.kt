package no.nav.dagpenger.behandling.modell.hendelser

import no.nav.dagpenger.behandling.modell.Meldekort
import no.nav.dagpenger.opplysning.Faktum
import no.nav.dagpenger.opplysning.Gyldighetsperiode
import no.nav.dagpenger.opplysning.Opplysninger
import no.nav.dagpenger.regel.beregning.Beregning.arbeidsdag
import no.nav.dagpenger.regel.beregning.Beregning.arbeidstimer
import no.nav.dagpenger.regel.beregning.Beregning.meldeperiodeBehandlet
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class MeldekortMottattHendelse(
    meldingsreferanseId: UUID,
    ident: String,
    private val meldekortId: UUID,
    private val meldekortFraOgmed: LocalDate,
    private val meldekortTilOgmed: LocalDate,
    private val arbeidsdager: Map<LocalDate, Int>,
    opprettet: LocalDateTime,
) : PersonHendelse(meldingsreferanseId, ident, opprettet) {
    private val opplysninger: Opplysninger =
        Opplysninger(
            *arbeidsdager
                .map { (dato, antall) ->
                    Faktum(arbeidsdag, verdi = true, Gyldighetsperiode(dato, dato))
                    Faktum(arbeidstimer, verdi = antall, Gyldighetsperiode(dato, dato))
                }.toTypedArray(),
            Faktum(meldeperiodeBehandlet, false, Gyldighetsperiode(meldekortFraOgmed, meldekortTilOgmed)),
        )

    fun somMeldekort(): Meldekort =
        Meldekort(
            meldekortId = meldekortId,
            opplysninger = opplysninger,
        )
}
