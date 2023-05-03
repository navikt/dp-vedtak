package no.nav.dagpenger.vedtak.mediator.vedtak.iverksett

import kotlinx.coroutines.runBlocking
import no.nav.dagpenger.vedtak.mediator.vedtak.iverksett.models.BehandlingsdetaljerDto
import no.nav.dagpenger.vedtak.mediator.vedtak.iverksett.models.Datoperiode
import no.nav.dagpenger.vedtak.mediator.vedtak.iverksett.models.FagsakdetaljerDto
import no.nav.dagpenger.vedtak.mediator.vedtak.iverksett.models.FagsakdetaljerDto.Stønadstype.DAGPENGER
import no.nav.dagpenger.vedtak.mediator.vedtak.iverksett.models.IverksettDagpengerDto
import no.nav.dagpenger.vedtak.mediator.vedtak.iverksett.models.SøkerDto
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

    override fun vedtaktFattet(ident: String, vedtakFattet: VedtakObserver.VedtakFattet) {
        val iverksettDagpengerdDto = iverksettDagpengerdDto(vedtakFattet, ident)

        runBlocking {
            iverksettClient.iverksett(iverksettDagpengerdDto)
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
        søkerDto = SøkerDto(
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
            utbetalinger = emptyList(),
            vedtaksperioder = listOf(
                VedtaksperiodeDagpengerDto(
                    periode = Datoperiode(
                        fom = vedtakFattet.virkningsdato,
                        tom = LocalDate.MAX,
                    ),
                    aktivitet = VedtaksperiodeDagpengerDto.Aktivitet.IKKEAKTIVITETSPLIKT,
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
            behandlingÅrsak = BehandlingsdetaljerDto.Behandlingårsak.SØKNAD,
            vilkårsvurderinger = emptyList(),
            aktivitetspliktInntrefferDato = null,
            kravMottatt = null,
            getårsakRevurdering = null,
        )
}
