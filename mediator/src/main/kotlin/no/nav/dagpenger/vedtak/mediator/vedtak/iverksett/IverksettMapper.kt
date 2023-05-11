package no.nav.dagpenger.vedtak.mediator.vedtak.iverksett

import no.nav.dagpenger.vedtak.iverksetting.mediator.behovløsere.models.BehandlingType
import no.nav.dagpenger.vedtak.iverksetting.mediator.behovløsere.models.BehandlingsdetaljerDto
import no.nav.dagpenger.vedtak.iverksetting.mediator.behovløsere.models.BehandlingÅrsak
import no.nav.dagpenger.vedtak.iverksetting.mediator.behovløsere.models.IverksettDagpengerdDto
import no.nav.dagpenger.vedtak.iverksetting.mediator.behovløsere.models.SakDto
import no.nav.dagpenger.vedtak.iverksetting.mediator.behovløsere.models.SøkerDto
import no.nav.dagpenger.vedtak.iverksetting.mediator.behovløsere.models.VedtaksdetaljerDagpengerDto
import no.nav.dagpenger.vedtak.iverksetting.mediator.behovløsere.models.VedtaksperiodeDagpengerDto
import no.nav.dagpenger.vedtak.iverksetting.mediator.behovløsere.models.Vedtaksresultat
import no.nav.dagpenger.vedtak.modell.vedtak.VedtakObserver
import java.util.UUID

internal fun VedtakObserver.VedtakFattet.tilIverksettDto(ident: String) = IverksettDagpengerdDto(
    sak = SakDto(
        sakId = UUID.randomUUID(),
    ),
    behandling = behandlingsdetaljerDto(this),
    søker = SøkerDto(
        personIdent = ident,
    ),
    vedtak = vedtaksdetaljerDagpengerDto(this),
)
private fun behandlingsdetaljerDto(vedtakFattet: VedtakObserver.VedtakFattet) =
    BehandlingsdetaljerDto(
        behandlingId = vedtakFattet.behandlingId,
        behandlingType = BehandlingType.FØRSTEGANGSBEHANDLING,
        behandlingÅrsak = BehandlingÅrsak.SØKNAD,
    )

private fun vedtaksdetaljerDagpengerDto(vedtakFattet: VedtakObserver.VedtakFattet) =
    VedtaksdetaljerDagpengerDto(
        vedtakstidspunkt = vedtakFattet.vedtakstidspunkt,
        resultat = when (vedtakFattet.utfall) {
            VedtakObserver.VedtakFattet.Utfall.Innvilget -> Vedtaksresultat.INNVILGET
            VedtakObserver.VedtakFattet.Utfall.Avslått -> Vedtaksresultat.AVSLÅTT
        },

        saksbehandlerId = "DIGIDAG",
        beslutterId = "DIGIDAG",
        vedtaksperioder = listOf(
            VedtaksperiodeDagpengerDto(
                fraOgMedDato = vedtakFattet.virkningsdato,
            ),
        ),
    )
