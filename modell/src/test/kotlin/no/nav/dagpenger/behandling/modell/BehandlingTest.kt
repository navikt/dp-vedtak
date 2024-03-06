package no.nav.dagpenger.behandling.modell

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import no.nav.dagpenger.behandling.modell.BehandlingObservatør.BehandlingEvent
import no.nav.dagpenger.behandling.modell.hendelser.SøknadInnsendtHendelse
import no.nav.dagpenger.opplysning.Faktum
import no.nav.dagpenger.opplysning.Gyldighetsperiode
import no.nav.dagpenger.opplysning.Opplysningstype
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class BehandlingTest {
    private val ident = "123456789011"
    private val søknadId = UUIDv7.ny()
    private val søknadInnsendtHendelse =
        SøknadInnsendtHendelse(
            søknadId = søknadId,
            ident = ident,
            meldingsreferanseId = søknadId,
            gjelderDato = LocalDate.now(),
        )
    private val testObservatør = TestObservatør()
    private val tidligereOpplysning = Opplysningstype.somDesimaltall("opplysning-fra-tidligere-behandling")

    @Test
    fun `Behandling basert på tidligere behandlinger`() {
        val behandlingskjede = behandlingskjede(5, søknadInnsendtHendelse)
        behandlingskjede.opplysninger().finnAlle() shouldHaveSize 5
        behandlingskjede.opplysninger().finnAlle().map {
            it.verdi
        } shouldContainExactly listOf(1.0, 2.0, 3.0, 4.0, 5.0)
    }

    private fun behandlingskjede(
        antall: Int,
        hendelse: SøknadInnsendtHendelse,
    ): Behandling {
        var fomTom = LocalDate.now()
        var forrigeBehandling: Behandling? = null
        for (nummer in 1..antall) {
            val behandling =
                Behandling(
                    behandler = hendelse,
                    opplysninger = listOf(Faktum(tidligereOpplysning, nummer.toDouble(), Gyldighetsperiode(fomTom, fomTom))),
                    basertPå = forrigeBehandling?.let { listOf(it) } ?: emptyList(),
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
                behandler = søknadInnsendtHendelse,
                opplysninger = emptyList(),
            ).also {
                it.leggTilObservatør(testObservatør)
            }

        behandling.håndter(søknadInnsendtHendelse)
        testObservatør.behandlingOpprettet shouldNotBe null
        testObservatør.behandlingOpprettet.ident shouldBe ident
        testObservatør.behandlingOpprettet.søknadId shouldBe søknadId
    }
}

private class TestObservatør : BehandlingObservatør {
    lateinit var behandlingOpprettet: BehandlingEvent.Opprettet

    override fun behandlingOpprettet(behandlingOpprettet: BehandlingEvent.Opprettet) {
        this.behandlingOpprettet = behandlingOpprettet
    }

    override fun forslagTilVedtak(forslagTilVedtak: BehandlingEvent.ForslagTilVedtak) {}
}
