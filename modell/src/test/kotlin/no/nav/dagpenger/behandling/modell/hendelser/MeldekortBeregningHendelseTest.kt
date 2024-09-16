package no.nav.dagpenger.behandling.modell.hendelser

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import no.nav.dagpenger.behandling.hjelpere.mai
import no.nav.dagpenger.behandling.modell.Behandling
import no.nav.dagpenger.opplysning.Faktum
import no.nav.dagpenger.opplysning.Gyldighetsperiode
import no.nav.dagpenger.opplysning.Opplysninger
import no.nav.dagpenger.opplysning.Regelkjøringsrapport
import no.nav.dagpenger.opplysning.verdier.Beløp
import no.nav.dagpenger.regel.TapAvArbeidsinntektOgArbeidstid.fastsattVanligArbeidstid
import no.nav.dagpenger.regel.beregning.Beregning.terskel
import no.nav.dagpenger.regel.fastsetting.DagpengenesStørrelse.sats
import no.nav.dagpenger.regel.fastsetting.Dagpengeperiode.antallStønadsuker
import no.nav.dagpenger.regel.fastsetting.Egenandel.egenandel
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID

class MeldekortBeregningHendelseTest {
    private val testIdent = "12345678901"

    @Test
    fun `meldekort og sånt`() {
        val behandling1 = Behandling(søknad, emptyList())
        behandling1.håndter(søknad)
        val behandling2 = Behandling(meldekort, emptyList(), listOf(behandling1))

        behandling2.håndter(meldekort)
        meldekort.aktivitetsteller() shouldBe 3
    }

    val meldekort =
        MeldekortBeregningHendelse(
            meldingsreferanseId = UUID.randomUUID(),
            ident = testIdent,
            meldekortId = UUID.randomUUID(),
            meldekortFraOgmed = 1.mai,
            meldekortTilOgmed = 14.mai,
            arbeidsdager =
                mapOf(
                    1.mai to 7,
                    2.mai to 0,
                    3.mai to 0,
                    4.mai to 0,
                    5.mai to 0,
                    6.mai to 0,
                    7.mai to 0,
                    8.mai to 0,
                    9.mai to 0,
                    10.mai to 0,
                    11.mai to 0,
                    12.mai to 0,
                    13.mai to 0,
                    14.mai to 0,
                ),
            opprettet = LocalDateTime.now(),
        )

    val søknad =
        mockk<SøknadInnsendtHendelse>(relaxed = true).apply {
            every { ident } returns testIdent

            val opplysninger = slot<Opplysninger>()
            every { evaluer(capture(opplysninger)) } answers {
                val gyldighetsperiode = Gyldighetsperiode(1.mai, 14.mai)
                listOf(
                    Faktum(SøknadInnsendtHendelse.fagsakIdOpplysningstype, 1, gyldighetsperiode),
                    Faktum(antallStønadsuker, 52, gyldighetsperiode),
                    Faktum(sats, Beløp(100), gyldighetsperiode),
                    Faktum(fastsattVanligArbeidstid, 37.5, gyldighetsperiode),
                    Faktum(terskel, 0.5, gyldighetsperiode),
                    Faktum(egenandel, Beløp(3000.0), gyldighetsperiode),
                ).forEach {
                    opplysninger.captured.leggTil(it)
                }

                Regelkjøringsrapport(
                    kjørteRegler = emptyList(),
                    mangler = emptySet(),
                    informasjonsbehov = emptyMap(),
                )
            }

            every { kontrollpunkter() } returns emptyList()
            every { harYtelse(any()) } returns true
            every { kontekstMap() } returns emptyMap()
        }
}
