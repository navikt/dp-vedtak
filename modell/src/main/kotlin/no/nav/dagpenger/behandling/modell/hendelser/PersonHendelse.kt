package no.nav.dagpenger.behandling.modell.hendelser

import no.nav.dagpenger.aktivitetslogg.Aktivitetslogg
import no.nav.dagpenger.aktivitetslogg.AktivitetsloggHendelse
import no.nav.dagpenger.aktivitetslogg.IAktivitetslogg
import no.nav.dagpenger.aktivitetslogg.SpesifikkKontekst
import java.time.LocalDateTime
import java.util.UUID

abstract class PersonHendelse(
    private val meldingsreferanseId: UUID,
    private val ident: String,
    val opprettet: LocalDateTime,
    private val aktivitetslogg: Aktivitetslogg = Aktivitetslogg(),
) : AktivitetsloggHendelse,
    IAktivitetslogg by aktivitetslogg {
    init {
        // TODO: Fjern denne når vi har fikset oppsett av aktivitetslogg
        // aktivitetslogg.kontekst(this)
    }

    override fun ident() = ident

    override fun toSpesifikkKontekst(): SpesifikkKontekst =
        SpesifikkKontekst(this.javaClass.simpleName, mapOf("ident" to ident) + kontekstMap())

    fun toLogString(): String = aktivitetslogg.toString()

    override fun meldingsreferanseId() = meldingsreferanseId

    open fun kontekstMap(): Map<String, String> = emptyMap()
}
