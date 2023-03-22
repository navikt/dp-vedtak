package no.nav.dagpenger.vedtak.modell.rapportering

import no.nav.dagpenger.vedtak.modell.Aktivitetslogg
import no.nav.dagpenger.vedtak.modell.Behandling
import no.nav.dagpenger.vedtak.modell.Person
import no.nav.dagpenger.vedtak.modell.SpesifikkKontekst
import no.nav.dagpenger.vedtak.modell.Vedtak
import no.nav.dagpenger.vedtak.modell.fastsettelse.Paragraf_4_15_Forbruk
import no.nav.dagpenger.vedtak.modell.hendelser.Hendelse
import no.nav.dagpenger.vedtak.modell.hendelser.Rapporteringshendelse
import no.nav.dagpenger.vedtak.modell.mengde.Enhet.Companion.arbeidsdager
import no.nav.dagpenger.vedtak.modell.mengde.Tid
import no.nav.dagpenger.vedtak.modell.rapportering.Rapporteringsbehandling.Behandlet
import no.nav.dagpenger.vedtak.modell.rapportering.Rapporteringsbehandling.Fastsetter
import no.nav.dagpenger.vedtak.modell.rapportering.Rapporteringsbehandling.VurderUtfall
import no.nav.dagpenger.vedtak.modell.rapportering.Rapporteringsbehandling.VurdererVilkår
import no.nav.dagpenger.vedtak.modell.vilkår.LøpendeStønadsperiodeVilkår
import no.nav.dagpenger.vedtak.modell.vilkår.Vilkårsvurdering.Companion.oppfylt
import no.nav.dagpenger.vedtak.modell.vilkår.Vilkårsvurdering.Companion.vurdert
import no.nav.dagpenger.vedtak.modell.visitor.FastsettelseVisitor
import java.time.LocalDate
import java.util.UUID

internal class Rapporteringsbehandling(
    private val person: Person,
    private val rapporteringsId: UUID,
    private val behandlingsId: UUID = UUID.randomUUID(),
    private val tellendeDager: MutableList<Dag> = mutableListOf(),
    tilstand: Tilstand<Rapporteringsbehandling> = ForberedendeFakta,
    aktivitetslogg: Aktivitetslogg = Aktivitetslogg(),
) : Behandling<Rapporteringsbehandling>(
    person = person,
    behandlingsId = behandlingsId,
    hendelseId = rapporteringsId,
    tilstand = tilstand,
    vilkårsvurdering = LøpendeStønadsperiodeVilkår(person),
    aktivitetslogg = aktivitetslogg,
) {
    override val fastsettelser by lazy {
        listOf(Paragraf_4_15_Forbruk())
    }

    object ForberedendeFakta : Tilstand.ForberedBehandling<Rapporteringsbehandling>() {
        override fun håndter(rapporteringsHendelse: Rapporteringshendelse, behandling: Rapporteringsbehandling) {
            behandling.tellendeDager.addAll(
                TellendeDager(behandling.person, rapporteringsHendelse.somPeriode()).tellendeDager(),
            )
            behandling.endreTilstand(VurdererVilkår, rapporteringsHendelse)
        }
    }

    object VurdererVilkår : Tilstand.VurdererVilkår<Rapporteringsbehandling>() {
        override fun entering(hendelse: Hendelse, behandling: Rapporteringsbehandling) {
            require(hendelse is Rapporteringshendelse) { "Hendelse er ikke rapporteringshendelse. Hendelsetype: ${hendelse.javaClass.simpleName}. Tilstand: $type" }
            behandling.vilkårsvurdering.håndter(hendelse, behandling.tellendeDager)
            if (behandling.vilkårsvurdering.vurdert()) {
                behandling.endreTilstand(VurderUtfall, hendelse)
            }
        }
    }

    object VurderUtfall : Tilstand.VurderUtfall<Rapporteringsbehandling>() {
        override fun entering(hendelse: Hendelse, behandling: Rapporteringsbehandling) {
            require(behandling.vilkårsvurdering.vurdert()) { "Vilkårsvurderinger må være ferdig vurdert på dette tidspunktet" }
            if (behandling.vilkårsvurdering.oppfylt()) {
                behandling.endreTilstand(Fastsetter, hendelse)
            } else {
                behandling.endreTilstand(Behandlet, hendelse)
            }
        }
    }

    object Fastsetter : Tilstand.Fastsetter<Rapporteringsbehandling>() {

        override fun entering(hendelse: Hendelse, behandling: Rapporteringsbehandling) {
            if (hendelse is Rapporteringshendelse) {
                behandling.fastsettelser.forEach { it.håndter(hendelse, behandling.tellendeDager) }
                behandling.endreTilstand(Behandlet, hendelse)
            }
        }
    }

    object Behandlet : Tilstand.Behandlet<Rapporteringsbehandling>() {
        override fun entering(hendelse: Hendelse, behandling: Rapporteringsbehandling) {
            behandling.opprettVedtak()
        }
    }

    private fun opprettVedtak() {
        person.leggTilVedtak(
            Vedtak.løpendeVedtak(
                behandlingId = behandlingsId,
                utfall = vilkårsvurdering.oppfylt(),
                virkningsdato = LocalDate.now(),
                forbruk = FastsattForbruk(fastsettelser).forbruk,
            ),
        )
    }

    override fun <T> implementasjon(block: Rapporteringsbehandling.() -> T): T = this.block()

    override fun toSpesifikkKontekst(): SpesifikkKontekst = SpesifikkKontekst(
        kontekstType = kontekstType,
        mapOf(
            "behandlingsId" to behandlingsId.toString(),
            "type" to this.javaClass.simpleName,
            "hendelse_uuid" to hendelseId.toString(),
        ),
    )

    fun håndter(rapporteringsHendelse: Rapporteringshendelse) {
        kontekst(rapporteringsHendelse, "Opprettet ny rapporteringsbehandling basert på rapporteringshendelse")
        tilstand.håndter(rapporteringsHendelse, this)
    }

    private class FastsattForbruk(fastsettelser: List<Paragraf_4_15_Forbruk>) : FastsettelseVisitor {

        var forbruk: Tid = 0.arbeidsdager

        init {
            fastsettelser.forEach { it.accept(this) }
        }

        override fun visitForbruk(forbruk: Tid) {
            this.forbruk += forbruk
        }
    }
}
