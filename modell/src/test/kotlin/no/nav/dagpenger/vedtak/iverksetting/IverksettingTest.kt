package no.nav.dagpenger.vedtak.iverksetting

import io.kotest.matchers.shouldBe
import no.nav.dagpenger.vedtak.iverksetting.Iverksetting.Tilstand.TilstandNavn.AvventerIverksetting
import no.nav.dagpenger.vedtak.iverksetting.Iverksetting.Tilstand.TilstandNavn.Iverksatt
import no.nav.dagpenger.vedtak.iverksetting.Iverksetting.Tilstand.TilstandNavn.Mottatt
import no.nav.dagpenger.vedtak.iverksetting.hendelser.IverksattHendelse
import no.nav.dagpenger.vedtak.iverksetting.hendelser.UtbetalingsvedtakFattetHendelse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

internal class IverksettingTest {

    private val ident = "12345678911"
    private val testObservatør = IverksettingObservatør()
    private val vedtakId = UUID.randomUUID()
    private val behandlingId = UUID.randomUUID()
    private val sakId = "SAKSNUMMER_1"
    private val vedtakstidspunkt = LocalDateTime.now()
    private val virkningsdato = LocalDate.now()
    private val forrigeBhandlingId = UUID.randomUUID()

    private lateinit var iverksetting: Iverksetting
    private val inspektør get() = IverksettingInspektør(iverksetting)

    @BeforeEach
    fun setup() {
        iverksetting = Iverksetting(vedtakId, ident).also {
            it.addObserver(testObservatør)
        }
    }

    @Test
    fun `Skal starte iverksetting når utbetalingsvedtak fattes med førstegangsutbetaling`() {
        iverksetting.håndter(
            UtbetalingsvedtakFattetHendelse(
                meldingsreferanseId = UUID.randomUUID(),
                ident = ident,
                vedtakId = vedtakId,
                behandlingId = behandlingId,
                sakId = sakId,
                vedtakstidspunkt = vedtakstidspunkt,
                virkningsdato = virkningsdato,
                forrigeBehandlingId = null,
                utbetalingsdager = utbetalingsdager(),
                utfall = UtbetalingsvedtakFattetHendelse.Utfall.Innvilget,
            ),
        )

        inspektør.tilstand.tilstandNavn shouldBe AvventerIverksetting

        iverksetting.håndter(
            IverksattHendelse(
                meldingsreferanseId = UUID.randomUUID(),
                ident = ident,
                iverksettingId = inspektør.iverksettingId,
                vedtakId = inspektør.vedtakId,
            ),
        )

        assertTilstander(
            Mottatt,
            AvventerIverksetting,
            Iverksatt,
        )
    }

    @Test
    fun `Skal starte iverksetting når utbetalingsvedtak fattes etter førstegangsutbetaling`() {
        iverksetting.håndter(
            UtbetalingsvedtakFattetHendelse(
                meldingsreferanseId = UUID.randomUUID(),
                ident = ident,
                vedtakId = vedtakId,
                behandlingId = behandlingId,
                sakId = sakId,
                vedtakstidspunkt = vedtakstidspunkt,
                virkningsdato = virkningsdato,
                forrigeBehandlingId = forrigeBhandlingId,
                utbetalingsdager = utbetalingsdager(),
                utfall = UtbetalingsvedtakFattetHendelse.Utfall.Innvilget,
            ),
        )

        inspektør.tilstand.tilstandNavn shouldBe AvventerIverksetting

        iverksetting.håndter(
            IverksattHendelse(
                meldingsreferanseId = UUID.randomUUID(),
                ident = ident,
                iverksettingId = inspektør.iverksettingId,
                vedtakId = inspektør.vedtakId,
            ),
        )

        assertTilstander(
            Mottatt,
            AvventerIverksetting,
            Iverksatt,
        )
    }

    private fun utbetalingsdager() = listOf(
        UtbetalingsvedtakFattetHendelse.Utbetalingsdag(dato = virkningsdato, beløp = 10.0),
    )

    private fun assertTilstander(vararg tilstander: Iverksetting.Tilstand.TilstandNavn) {
        tilstander.asList() shouldBe testObservatør.tilstander
    }
}
