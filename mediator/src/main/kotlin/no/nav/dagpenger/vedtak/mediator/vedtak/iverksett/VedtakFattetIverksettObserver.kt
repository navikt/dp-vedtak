package no.nav.dagpenger.vedtak.mediator.vedtak.iverksett

import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import no.nav.dagpenger.vedtak.mediator.vedtak.iverksett.models.BehandlingsdetaljerDto
import no.nav.dagpenger.vedtak.mediator.vedtak.iverksett.models.Datoperiode
import no.nav.dagpenger.vedtak.mediator.vedtak.iverksett.models.FagsakdetaljerDto
import no.nav.dagpenger.vedtak.mediator.vedtak.iverksett.models.FagsakdetaljerDto.Stønadstype.DAGPENGER
import no.nav.dagpenger.vedtak.mediator.vedtak.iverksett.models.IverksettDagpengerDto
import no.nav.dagpenger.vedtak.mediator.vedtak.iverksett.models.SøkerDto
import no.nav.dagpenger.vedtak.mediator.vedtak.iverksett.models.UtbetalingDto
import no.nav.dagpenger.vedtak.mediator.vedtak.iverksett.models.VedtaksdetaljerDagpengerDto
import no.nav.dagpenger.vedtak.mediator.vedtak.iverksett.models.VedtaksperiodeDagpengerDto
import no.nav.dagpenger.vedtak.modell.PersonObserver
import no.nav.dagpenger.vedtak.modell.vedtak.VedtakObserver
import no.nav.dagpenger.vedtak.modell.vedtak.VedtakObserver.VedtakFattet.Utfall.Avslått
import no.nav.dagpenger.vedtak.modell.vedtak.VedtakObserver.VedtakFattet.Utfall.Innvilget
import java.time.LocalDate
import java.util.UUID
import kotlin.random.Random

internal class VedtakFattetIverksettObserver(private val iverksettClient: IverksettClient) : PersonObserver {

    private val logger = KotlinLogging.logger { }

    override fun vedtaktFattet(ident: String, vedtakFattet: VedtakObserver.VedtakFattet) {
        val iverksettDagpengerdDto = iverksettDagpengerdDto(vedtakFattet, ident)

        if (vedtakFattet.utfall == Innvilget) {
            runBlocking {
                iverksettClient.iverksett(iverksettDagpengerdDto)
            }
        } else {
            logger.warn { "Kan ikke iverksette vedtak med id ${vedtakFattet.vedtakId} den har annet utfall enn Innvilget" }
        }
    }

    private fun iverksettDagpengerdDto(
        vedtakFattet: VedtakObserver.VedtakFattet,
        ident: String,
    ) = IverksettDagpengerDto(
        fagsak = FagsakdetaljerDto(
            fagsakId = UUID.randomUUID(),
            eksternId = Random.nextLong(),
            stønadstype = DAGPENGER,
        ),
        behandling = behandlingsdetaljerDto(vedtakFattet),
        søker = SøkerDto(
            personIdent = ident,
            barn = emptyList(),
            tilhørendeEnhet = "Jaja",
            adressebeskyttelse = null,
        ),
        vedtak = vedtaksdetaljerDagpengerDto(vedtakFattet),
        forrigeVedtak = null,
    )

    private fun vedtaksdetaljerDagpengerDto(vedtakFattet: VedtakObserver.VedtakFattet) =
        VedtaksdetaljerDagpengerDto(
            vedtakstidspunkt = vedtakFattet.vedtakstidspunkt,
            resultat = when (vedtakFattet.utfall) {
                Innvilget -> VedtaksdetaljerDagpengerDto.Resultat.INNVILGET
                Avslått -> VedtaksdetaljerDagpengerDto.Resultat.AVSLÅTT
            },
            opphørårsak = null,
            avslagårsak = null,
            saksbehandlerId = "DIGIDAG",
            beslutterId = "DIGIDAG",
            utbetalinger = listOf(
                UtbetalingDto(
                    beløp = 100,
                    inntekt = 5000,
                    samordningsfradrag = 0,
                    inntektsreduksjon = 0,
                    periode = Datoperiode(vedtakFattet.virkningsdato, LocalDate.MAX),
                ),

            ),
            vedtaksperioder = listOf(
                VedtaksperiodeDagpengerDto(
                    periode = Datoperiode(
                        fom = vedtakFattet.virkningsdato,
                        tom = LocalDate.MAX,
                    ),
                    aktivitet = VedtaksperiodeDagpengerDto.Aktivitet.IKKE_AKTIVITETSPLIKT,
                    periodeType = VedtaksperiodeDagpengerDto.PeriodeType.HOVEDPERIODE,
                ),
            ),
            tilbakekreving = null,
            brevmottakere = emptyList(),
        )

    private fun behandlingsdetaljerDto(vedtakFattet: VedtakObserver.VedtakFattet) =
        BehandlingsdetaljerDto(
            behandlingId = vedtakFattet.behandlingId,
            forrigeBehandlingId = null,
            eksternId = Random.nextLong(),
            behandlingType = BehandlingsdetaljerDto.BehandlingType.FØRSTEGANGSBEHANDLING,
            behandlingÅrsak = BehandlingsdetaljerDto.Behandlingårsak.MIGRERING,
            vilkårsvurderinger = emptyList(),
            aktivitetspliktInntrefferDato = null,
            kravMottatt = null,
            getårsakRevurdering = null,
        )
}
