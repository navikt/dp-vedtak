package no.nav.dagpenger.behandling.modell

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.date.shouldBeWithin
import io.kotest.matchers.maps.shouldContain
import io.kotest.matchers.shouldBe
import no.nav.dagpenger.behandling.hjelpere.mai
import no.nav.dagpenger.behandling.modell.Behandling.TilstandType.Ferdig
import no.nav.dagpenger.behandling.modell.Behandling.TilstandType.UnderBehandling
import no.nav.dagpenger.behandling.modell.Behandling.TilstandType.UnderOpprettelse
import no.nav.dagpenger.behandling.modell.hendelser.SøknadInnsendtHendelse
import no.nav.dagpenger.opplysning.Faktum
import no.nav.dagpenger.opplysning.Gyldighetsperiode
import no.nav.dagpenger.opplysning.Opplysninger
import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.uuid.UUIDv7
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime

internal class BehandlingTest {
    private val ident = "123456789011"
    private val søknadId = UUIDv7.ny()
    private val søknadInnsendtHendelse =
        SøknadInnsendtHendelse(
            søknadId = søknadId,
            ident = ident,
            meldingsreferanseId = søknadId,
            gjelderDato = LocalDate.now(),
            fagsakId = 1,
            opprettet = LocalDateTime.now(),
        )

    private companion object {
        val tidligereOpplysning = Opplysningstype.somDesimaltall("opplysning-fra-tidligere-behandling")
        val rettighet = Opplysningstype.somBoolsk("rettPåDagpenger")
        val meldekortPeriode = Opplysningstype.somBoolsk("meldekortperiode")
    }

    @Test
    fun `Behandling basert på tidligere behandlinger`() {
        val behandlingskjede = behandlingskjede(5, søknadInnsendtHendelse)
        behandlingskjede.opplysninger().finnAlle() shouldHaveSize 5
        behandlingskjede.opplysninger().finnAlle().map {
            it.verdi
        } shouldContainExactly listOf(1.0, 2.0, 3.0, 4.0, 5.0)
    }

    @Test
    @Disabled("Vi må vite mer om hva som skal skje her")
    fun `Behandling for søknader er ikke ferdig behandlet, og vi har meldekort i kø`() {
        val meldekorthendelse = søknadInnsendtHendelse

        // Behandlingskøa ser slik ut:
        val søknadsbehandling = Behandling(søknadInnsendtHendelse, emptyList())
        val meldekort1 = Behandling(meldekorthendelse, meldekortOpplysninger(1.mai, 14.mai), listOf(søknadsbehandling))
        val meldekort2 = Behandling(meldekorthendelse, meldekortOpplysninger(15.mai, 31.mai), listOf(meldekort1))

        søknadsbehandling.håndter(søknadInnsendtHendelse)
    }

    private fun meldekortOpplysninger(
        fom: LocalDate,
        tom: LocalDate,
    ) = listOf(Faktum(meldekortPeriode, true, Gyldighetsperiode(fom, tom)))

    private fun behandlingskjede(
        antall: Int,
        hendelse: SøknadInnsendtHendelse,
    ): Behandling {
        var fomTom = LocalDate.now()
        var forrigeBehandling: Behandling? = null
        for (nummer in 1..antall) {
            val behandling =
                Behandling.rehydrer(
                    behandlingId = UUIDv7.ny(),
                    behandler = hendelse,
                    gjeldendeOpplysninger =
                        Opplysninger(
                            listOf(
                                Faktum(
                                    tidligereOpplysning,
                                    nummer.toDouble(),
                                    Gyldighetsperiode(fomTom, fomTom),
                                ),
                            ),
                        ),
                    basertPå = forrigeBehandling?.let { listOf(it) } ?: emptyList(),
                    tilstand = Ferdig,
                    sistEndretTilstand = LocalDateTime.now(),
                    avklaringer = emptyList(),
                )
            forrigeBehandling = behandling
            // TODO: Det burde eksplodere uten denne
            //  fomTom = fomTom.plusDays(1)
        }
        return forrigeBehandling!!
    }

    @Test
    fun `behandling sender ut behandling opprettet eventer `() {
        val behandling =
            Behandling(
                behandler =
                    søknadInnsendtHendelse.also {
                        søknadInnsendtHendelse.kontekst(it)
                    },
                opplysninger = emptyList(),
            )

        behandling.håndter(søknadInnsendtHendelse)
        søknadInnsendtHendelse.hendelse() shouldHaveSize 1
        val hendelse = søknadInnsendtHendelse.hendelse().first()
        hendelse.type.name shouldBe "behandling_opprettet"
        val kontekst = hendelse.kontekst()
        kontekst.shouldContain("behandlingId", behandling.behandlingId.toString())
        kontekst.shouldContain("søknadId", søknadId.toString())
        kontekst.shouldContain("ident", ident)
    }

    @Test
    fun `behandling varsler om endret tilstand`() {
        val behandling =
            Behandling(
                behandler =
                    søknadInnsendtHendelse.also {
                        søknadInnsendtHendelse.kontekst(it)
                    },
                opplysninger = emptyList(),
            )

        val observatør = TestObservatør().also { behandling.registrer(it) }
        behandling.håndter(søknadInnsendtHendelse)

        observatør.endretTilstandEventer shouldHaveSize 1
        observatør.endretTilstandEventer.first().run {
            forrigeTilstand shouldBe UnderOpprettelse
            gjeldendeTilstand shouldBe UnderBehandling
            forventetFerdig.shouldBeWithin(Duration.ofHours(5), LocalDateTime.now())
        }
    }

    private class TestObservatør : BehandlingObservatør {
        val endretTilstandEventer = mutableListOf<BehandlingObservatør.BehandlingEndretTilstand>()

        override fun endretTilstand(event: BehandlingObservatør.BehandlingEndretTilstand) {
            endretTilstandEventer.add(event)
        }
    }
}
