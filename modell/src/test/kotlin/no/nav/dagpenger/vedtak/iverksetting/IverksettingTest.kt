package no.nav.dagpenger.vedtak.iverksetting

import no.nav.dagpenger.vedtak.iverksetting.hendelser.VedtakFattetHendelse
import org.junit.jupiter.api.Test
import java.util.UUID

class IverksettingTest {

    private val ident = "12345678911"
    private val testObservatør = TestObservatør()

    @Test
    fun `Skal starte iverksetting når vedtak fattes`() {
        val vedtakId = UUID.randomUUID()
        val iverksetting = Iverksetting(vedtakId)
        iverksetting.addObserver(testObservatør)
        iverksetting.håndter(
            VedtakFattetHendelse(ident = ident, vedtakId = vedtakId),
        )

    }

    private class TestObservatør : IverksettingObserver {
        override fun iverksettingTilstandEndret(event: IverksettingObserver.IverksettingEndretTilstandEvent) {

        }
    }
}
