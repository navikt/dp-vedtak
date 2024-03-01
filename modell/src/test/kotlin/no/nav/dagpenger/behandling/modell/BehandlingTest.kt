package no.nav.dagpenger.behandling.modell

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import no.nav.dagpenger.behandling.modell.hendelser.SøknadInnsendtHendelse
import no.nav.dagpenger.opplysning.Opplysninger
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

    @Test
    fun `behandling sender ut behandling opprettet eventer `() {
        val testObservatør = TestObservatør()
        val behandling =
            Behandling(
                behandler = søknadInnsendtHendelse,
                opplysninger = Opplysninger(opplysninger = listOf()),
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
