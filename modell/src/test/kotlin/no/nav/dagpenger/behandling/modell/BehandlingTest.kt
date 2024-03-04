package no.nav.dagpenger.behandling.modell

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import no.nav.dagpenger.behandling.modell.hendelser.SøknadInnsendtHendelse
import no.nav.dagpenger.opplysning.Faktum
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
    val tidligerOpplysning = Opplysningstype.somDesimaltall("opplysning-fra-tidliger-behandling")
    private val tidligerBehandling =
        Behandling(
            behandler = søknadInnsendtHendelse,
            opplysninger =
                listOf(
                    Faktum(
                        tidligerOpplysning,
                        0.5,
                    ),
                ),
        )

    private val testObservatør = TestObservatør()

    @Test
    fun `Behandling basert på tidligere behandlinger`() {
        val behandling =
            Behandling(
                behandler = søknadInnsendtHendelse,
                opplysninger = emptyList(),
                basertPå = listOf(tidligerBehandling),
            )

        behandling.opplysninger().har(tidligerOpplysning) shouldBe true
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
    lateinit var behandlingOpprettet: BehandlingObservatør.BehandlingOpprettet

    override fun behandlingOpprettet(behandlingOpprettet: BehandlingObservatør.BehandlingOpprettet) {
        this.behandlingOpprettet = behandlingOpprettet
    }

    override fun behandlingAvsluttet(behandlingAvsluttet: BehandlingObservatør.BehandlingAvsluttet) {}
}
