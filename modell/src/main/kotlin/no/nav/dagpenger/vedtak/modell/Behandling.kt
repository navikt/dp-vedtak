package no.nav.dagpenger.vedtak.modell

import no.nav.dagpenger.aktivitetslogg.Aktivitet
import no.nav.dagpenger.aktivitetslogg.Aktivitetskontekst
import no.nav.dagpenger.aktivitetslogg.SpesifikkKontekst
import no.nav.dagpenger.opplysning.Opplysninger
import no.nav.dagpenger.opplysning.Regelkjøring
import no.nav.dagpenger.opplysning.Regelsett
import no.nav.dagpenger.regel.RettTilDagpenger
import no.nav.dagpenger.vedtak.modell.hendelser.SøkerHendelse
import no.nav.dagpenger.vedtak.modell.hendelser.SøknadInnsendtHendelse
import java.util.UUID

class Behandling private constructor(
    private val behandlingId: UUID,
    private val behandler: SøkerHendelse,
    private val opplysninger: Opplysninger,
    private vararg val regelsett: Regelsett,
) : Aktivitetskontekst {
    constructor(
        behandler: SøkerHendelse,
        opplysninger: Opplysninger,
        vararg regelsett: Regelsett,
    ) : this(UUIDv7.ny(), behandler, opplysninger, *regelsett)

    private val regelkjøring = Regelkjøring(behandler.gjelderDato, opplysninger, *regelsett)

    private fun informasjonsbehov() = regelkjøring.trenger(RettTilDagpenger.kravPåDagpenger)

    fun håndter(hendelse: SøknadInnsendtHendelse) {
        hendelse.kontekst(this)
        informasjonsbehov().forEach {
            hendelse.behov(
                OpplysningBehov(it.id),
                "Trenger en opplysning",
            )
        }
    }

    override fun toSpesifikkKontekst() = SpesifikkKontekst("Behandling", mapOf("behandlingId" to behandlingId.toString()))
}

data class OpplysningBehov(override val name: String) : Aktivitet.Behov.Behovtype
