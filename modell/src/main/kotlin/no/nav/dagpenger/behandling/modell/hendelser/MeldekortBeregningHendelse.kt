package no.nav.dagpenger.behandling.modell.hendelser

import no.nav.dagpenger.avklaring.Avklaring
import no.nav.dagpenger.avklaring.Avklaringer
import no.nav.dagpenger.avklaring.Kontrollpunkt
import no.nav.dagpenger.behandling.modell.Meldekort
import no.nav.dagpenger.opplysning.Faktum
import no.nav.dagpenger.opplysning.Gyldighetsperiode
import no.nav.dagpenger.opplysning.LesbarOpplysninger
import no.nav.dagpenger.opplysning.Opplysninger
import no.nav.dagpenger.opplysning.Regelkjøringsrapport
import no.nav.dagpenger.opplysning.verdier.Beløp
import no.nav.dagpenger.regel.beregning.Beregning.arbeidsdag
import no.nav.dagpenger.regel.beregning.Beregning.arbeidstimer
import no.nav.dagpenger.regel.beregning.Beregning.forbruk
import no.nav.dagpenger.regel.beregning.Beregning.meldeperiodeBehandlet
import no.nav.dagpenger.regel.beregning.BeregningsperiodeFabrikk
import no.nav.dagpenger.regel.fastsetting.Dagpengeperiode.antallStønadsuker
import no.nav.dagpenger.regel.fastsetting.Dagpengeperiode.gjenståendeStønadsdager
import no.nav.dagpenger.regel.fastsetting.Egenandel.egenandel
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class MeldekortBeregningHendelse(
    meldingsreferanseId: UUID,
    override val ident: String,
    private val meldekortId: UUID,
    private val meldekortFraOgmed: LocalDate,
    private val meldekortTilOgmed: LocalDate,
    private val arbeidsdager: Map<LocalDate, Int>,
    opprettet: LocalDateTime,
) : PersonHendelse(meldingsreferanseId, ident, opprettet),
    NoeSomBehandlingKanKalle {
    private val opplysninger: Opplysninger =
        Opplysninger(
            *arbeidsdager
                .flatMap { (dato, antall) ->
                    listOf(
                        Faktum(arbeidsdag, verdi = true, Gyldighetsperiode(dato, dato)),
                        Faktum(
                            arbeidstimer,
                            verdi = antall,
                            Gyldighetsperiode(dato, dato),
                        ),
                    )
                }.toTypedArray(),
            Faktum(meldeperiodeBehandlet, false, Gyldighetsperiode(meldekortFraOgmed, meldekortTilOgmed)),
        )

    private val meldekort: Meldekort =
        Meldekort(
            meldekortId = meldekortId,
            opplysninger = opplysninger,
        )

    override fun evaluer(opplysninger: Opplysninger): Regelkjøringsrapport {
        meldekort.opplysninger.finnAlle().forEach {
            opplysninger.leggTil(it)
        }

        val beregning =
            BeregningsperiodeFabrikk(
                meldekort.fraOgMed,
                meldekort.tilOgMed,
                opplysninger,
            ).lagBeregningsperiode()

        // Lagre dager som har blitt forbrukt
        beregning.forbruksdager.forEach {
            opplysninger.leggTil(Faktum(forbruk, true, Gyldighetsperiode(it.dato, it.dato)))
        }

        // Lagre gjenstående stønadsdager tilbake i opplysninger
        val utgangspunkt = opplysninger.finnAlle().find { it.opplysningstype == antallStønadsuker }!!.verdi as Int * 5
        val forbrukteDager = opplysninger.finnAlle().filter { it.opplysningstype == forbruk }.size
        val gjenståendeDager = utgangspunkt - forbrukteDager

        opplysninger.leggTil(Faktum(gjenståendeStønadsdager, gjenståendeDager, Gyldighetsperiode(fom = meldekort.tilOgMed)))

        // Lagre egenandel
        val egenandel = opplysninger.finnAlle().find { it.opplysningstype == egenandel }!!.verdi as Beløp

        return Regelkjøringsrapport(
            kjørteRegler = emptyList(),
            mangler = emptySet(),
            informasjonsbehov = emptyMap(),
        )
    }

    override fun kontrollpunkter(): List<Kontrollpunkt> = emptyList()

    override fun avklaringer(
        avklaringer: Avklaringer,
        opplysninger: LesbarOpplysninger,
    ): List<Avklaring> = emptyList()

    override fun harYtelse(opplysninger: LesbarOpplysninger): Boolean = true

    override fun kontekstMap(): Map<String, String> = emptyMap()
}
