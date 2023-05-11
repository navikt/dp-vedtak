package no.nav.dagpenger.vedtak.mediator.vedtak.iverksett

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import no.nav.dagpenger.vedtak.iverksetting.mediator.behovløsere.IverksettClient
import no.nav.dagpenger.vedtak.modell.vedtak.VedtakObserver
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

internal class VedtakFattetIverksettObserverTest {
    private val iverksettClientMock = mockk<IverksettClient>().also {
        coEvery { it.iverksett(any()) } just runs
    }
    private val vedtakFattetIverksettObserver = VedtakFattetIverksettObserver(iverksettClientMock)

    @Test
    fun `skal sende melding om fattet vedtak til iverksatt API`() {
        val vedtakId = UUID.randomUUID()
        val behandlingId = UUID.randomUUID()
        val vedtakstidspunkt = LocalDateTime.now()
        val virkningsdato = LocalDate.now()
        vedtakFattetIverksettObserver.vedtaktFattet(
            ident = "1234568901",
            VedtakObserver.VedtakFattet(
                vedtakId,
                vedtakstidspunkt = vedtakstidspunkt,
                behandlingId = behandlingId,
                virkningsdato = virkningsdato,
                utfall = VedtakObserver.VedtakFattet.Utfall.Innvilget,
            ),
        )

        coVerify(exactly = 1) { iverksettClientMock.iverksett(any()) }
    }
}
