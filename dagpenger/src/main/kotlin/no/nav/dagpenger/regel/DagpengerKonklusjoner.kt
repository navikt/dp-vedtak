import no.nav.dagpenger.behandling.konklusjon.Konklusjon
import no.nav.dagpenger.behandling.konklusjon.KonklusjonsStrategi
import no.nav.dagpenger.regel.Alderskrav.oppfyllerKravet
import no.nav.dagpenger.regel.KravPåDagpenger
import no.nav.dagpenger.regel.Minsteinntekt.minsteinntekt

enum class DagpengerUtredningStoppÅrsak(override val årsak: String) : Konklusjon {
    Minsteinntekt("Minsteinntekt"),
    Alder("Personen er for gammel rett og slett"),
    Innvilgelse("Personen har rett til dagpenger"),
}

val AvslagInntekt =
    KonklusjonsStrategi(DagpengerUtredningStoppÅrsak.Minsteinntekt) { opplysninger ->
        if (!opplysninger.har(minsteinntekt)) return@KonklusjonsStrategi false
        if (opplysninger.finnOpplysning(minsteinntekt).verdi) {
            return@KonklusjonsStrategi true
        } else {
            false
        }
    }

val AvslagAlder =
    KonklusjonsStrategi(DagpengerUtredningStoppÅrsak.Alder) { opplysninger ->
        if (!opplysninger.har(oppfyllerKravet)) return@KonklusjonsStrategi false
        if (opplysninger.finnOpplysning(oppfyllerKravet).verdi) {
            return@KonklusjonsStrategi true
        } else {
            false
        }
    }

val Innvilgelse =
    KonklusjonsStrategi(DagpengerUtredningStoppÅrsak.Innvilgelse) { opplysninger ->
        if (!opplysninger.har(KravPåDagpenger.kravPåDagpenger)) return@KonklusjonsStrategi false
        if (opplysninger.finnOpplysning(KravPåDagpenger.kravPåDagpenger).verdi) {
            return@KonklusjonsStrategi true
        } else {
            false
        }
    }
