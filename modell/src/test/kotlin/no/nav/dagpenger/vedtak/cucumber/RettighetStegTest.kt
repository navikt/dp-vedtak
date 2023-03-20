package no.nav.dagpenger.vedtak.cucumber

import com.fasterxml.jackson.annotation.JsonFormat
import io.cucumber.java8.No
import no.nav.dagpenger.vedtak.modell.Dagpengerettighet
import no.nav.dagpenger.vedtak.modell.Person
import no.nav.dagpenger.vedtak.modell.PersonIdentifikator.Companion.tilPersonIdentfikator
import no.nav.dagpenger.vedtak.modell.entitet.Timer
import no.nav.dagpenger.vedtak.modell.hendelser.SøknadAvslåttHendelse
import no.nav.dagpenger.vedtak.modell.hendelser.SøknadInnvilgetHendelse
import no.nav.dagpenger.vedtak.modell.mengde.Enhet.Companion.arbeidsuker
import no.nav.dagpenger.vedtak.modell.mengde.Stønadsperiode
import no.nav.dagpenger.vedtak.modell.mengde.Tid
import no.nav.dagpenger.vedtak.modell.visitor.PersonVisitor
import org.junit.jupiter.api.Assertions.assertEquals
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

class RettighetStegTest : No {
    private val datoformatterer = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    private lateinit var person: Person
    private lateinit var ident: String

    private val inspektør get() = Inspektør(person)

    init {
        Gitt("en ny hendelse om innvilget søknad") { søknadHendelse: SøknadInnvilgetHendelseCucumber ->
            ident = søknadHendelse.fødselsnummer
            person = Person(ident.tilPersonIdentfikator())
            person.håndter(
                SøknadInnvilgetHendelse(
                    ident = ident,
                    behandlingId = UUID.fromString(søknadHendelse.behandlingId),
                    virkningsdato = søknadHendelse.virkningsdato,
                    dagsats = søknadHendelse.dagsats.toBigDecimal(),
                    grunnlag = søknadHendelse.grunnlag.toBigDecimal(),
                    stønadsperiode = søknadHendelse.stønadsperiode.arbeidsuker,
                ),
            )
        }

        Gitt("en ny hendelse om avslått søknad") { søknadHendelse: SøknadAvslåttHendelseCucumber ->
            ident = søknadHendelse.fødselsnummer
            person = Person(ident.tilPersonIdentfikator())
            person.håndter(
                SøknadAvslåttHendelse(
                    ident = ident,
                    behandlingId = UUID.fromString(søknadHendelse.behandlingId),
                    virkningsdato = søknadHendelse.virkningsdato,
                ),
            )
        }

        Så("skal bruker ha {int} vedtak") { antallVedtak: Int ->
            assertEquals(antallVedtak, inspektør.antallVedtak)
        }

        Så("vedtaket har virkningsdato {string}") { virkningsdato: String ->
            assertEquals(LocalDate.parse(virkningsdato, datoformatterer), inspektør.virkningsdato)
        }

        Så(
            "vedtaket har dagsats på {int}, grunnlag {int} og stønadsperiode på {int} uker",
        ) { dagsats: Int, grunnlag: Int, stønadsperiode: Int ->
            assertEquals(dagsats.toBigDecimal(), inspektør.dagsats)
            assertEquals(grunnlag.toBigDecimal(), inspektør.grunnlag)
            assertEquals(stønadsperiode.arbeidsuker, inspektør.stønadsperiode)
        }
    }

    private data class SøknadAvslåttHendelseCucumber(
        val fødselsnummer: String,
        val behandlingId: String,
        val utfall: Boolean,
        @JsonFormat(pattern = "dd.MM.yyyy")
        val virkningsdato: LocalDate,
    )

    private data class SøknadInnvilgetHendelseCucumber(
        val fødselsnummer: String,
        val behandlingId: String,
        val utfall: Boolean,
        @JsonFormat(pattern = "dd.MM.yyyy")
        val virkningsdato: LocalDate,
        val dagsats: Int,
        val grunnlag: Int,
        val stønadsperiode: Int,
    )

    private class Inspektør(person: Person) : PersonVisitor {
        init {
            person.accept(this)
        }

        lateinit var stønadsperiode: Stønadsperiode
        lateinit var grunnlag: BigDecimal
        lateinit var dagsats: BigDecimal
        lateinit var virkningsdato: LocalDate
        var antallVedtak = 0
        lateinit var forbruk: Tid

        override fun postVisitVedtak(
            vedtakId: UUID,
            virkningsdato: LocalDate,
            vedtakstidspunkt: LocalDateTime,
            utfall: Boolean,
            gyldigTom: LocalDate?,
        ) {
            antallVedtak++
            this.virkningsdato = virkningsdato
        }

        override fun visitRammeVedtak(
            grunnlag: BigDecimal,
            dagsats: BigDecimal,
            stønadsperiode: Stønadsperiode,
            fastsattArbeidstidPerDag: Timer,
            dagpengerettighet: Dagpengerettighet,
            gyldigTom: LocalDate?,
        ) {
            this.grunnlag = grunnlag
            this.dagsats = dagsats
            this.stønadsperiode = stønadsperiode
        }

        override fun visitForbruk(forbruk: Tid) {
            this.forbruk = forbruk
        }
    }
}
