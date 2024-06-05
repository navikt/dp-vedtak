package no.nav.dagpenger.behandling.modell.hendelser

import no.nav.dagpenger.avklaring.Avklaring
import no.nav.dagpenger.avklaring.Avklaringer
import no.nav.dagpenger.avklaring.Kontrollpunkt
import no.nav.dagpenger.behandling.konklusjon.Konklusjon
import no.nav.dagpenger.behandling.konklusjon.KonklusjonsStrategi
import no.nav.dagpenger.behandling.modell.Behandling
import no.nav.dagpenger.opplysning.LesbarOpplysninger
import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.opplysning.Regelsett
import java.time.LocalDate
import java.util.UUID

// Baseklasse for alle hendelser som kan påvirke dagpengene til en person og må behandles
abstract class StartHendelse(
    val meldingsreferanseId: UUID,
    val ident: String,
    val eksternId: EksternId<*>,
    val skjedde: LocalDate,
    val fagsakId: Int,
) : PersonHendelse(meldingsreferanseId, ident) {
    val type: String = this.javaClass.simpleName

    override fun kontekstMap() =
        mapOf(
            "gjelderDato" to skjedde.toString(),
        ) + eksternId.kontekstMap()

    abstract fun regelsett(): List<Regelsett>

    abstract fun avklarer(): Opplysningstype<Boolean>

    abstract fun behandling(): Behandling

    abstract fun konklusjonStrategier(): List<KonklusjonsStrategi>

    abstract fun kontrollpunkter(): List<Kontrollpunkt>

    fun konklusjoner(opplysninger: LesbarOpplysninger): List<Konklusjon> {
        return konklusjonStrategier().mapNotNull { konklusjonsStrategi ->
            konklusjonsStrategi.evaluer(opplysninger)
        }
    }

    fun avklaringer(opplysninger: LesbarOpplysninger): List<Avklaring> {
        return Avklaringer(kontrollpunkter()).måAvklares(opplysninger)
    }
}
