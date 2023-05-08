package no.nav.dagpenger.vedtak.iverksetting

import io.kotest.matchers.shouldBe
import no.nav.dagpenger.vedtak.iverksetting.Iverksetting.Tilstand.TilstandNavn.AvventerIverksetting
import no.nav.dagpenger.vedtak.iverksetting.Iverksetting.Tilstand.TilstandNavn.Mottatt
import no.nav.dagpenger.vedtak.iverksetting.hendelser.VedtakFattetHendelse
import no.nav.dagpenger.vedtak.modell.Aktivitetslogg.Aktivitet.Behov.Behovtype
import org.junit.jupiter.api.Test
import java.util.UUID

class IverksettingTest {

    private val ident = "12345678911"
    private val testObservatør = TestObservatør()
    private val vedtakId = UUID.randomUUID()
    private val iverksetting = Iverksetting(vedtakId).also {
        it.addObserver(testObservatør)
    }
    private val inspektør get() = IverksettingInspektør(iverksetting)

    @Test
    fun `Skal starte iverksetting når vedtak fattes`() {
        iverksetting.håndter(
            VedtakFattetHendelse(ident = ident, vedtakId = vedtakId),
        )

        assertBehovDetaljer(
            Behovtype.Iverksett,
        )

        assertTilstander(
            Mottatt,
            AvventerIverksetting,
        )
    }

    private class TestObservatør : IverksettingObserver {
        val tilstander = mutableListOf<Iverksetting.Tilstand.TilstandNavn>().also {
            it.add(Mottatt)
        }
        override fun iverksettingTilstandEndret(event: IverksettingObserver.IverksettingEndretTilstandEvent) {
            tilstander.add(event.gjeldendeTilstand)
        }
    }

    private fun assertTilstander(vararg tilstander: Iverksetting.Tilstand.TilstandNavn) {
        tilstander.asList() shouldBe testObservatør.tilstander
    }

    protected fun assertBehovDetaljer(type: Behovtype, detaljer: Set<String> = emptySet()) {
        val behov = inspektør.innsendingLogg.behov().find { behov ->
            behov.type == type
        } ?: throw AssertionError("Fant ikke behov ${type.name} i etterspurte behov")

        val forventet = detaljer + setOf("iverksettingId", "vedtakId", "tilstand")
        val faktisk = behov.detaljer().keys + behov.kontekster.flatMap { it.kontekstMap.keys }

        forventet shouldBe faktisk
    }
}
