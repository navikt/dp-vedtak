package no.nav.dagpenger.regel

import no.nav.dagpenger.avklaring.Kontrollpunkt
import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.opplysning.Regelsett
import no.nav.dagpenger.opplysning.regel.alle
import no.nav.dagpenger.opplysning.regel.dato.finnDagensDato
import no.nav.dagpenger.opplysning.regel.enAv
import no.nav.dagpenger.opplysning.regel.fraOgMed
import no.nav.dagpenger.opplysning.tekstId
import no.nav.dagpenger.regel.Minsteinntekt.minsteinntekt
import no.nav.dagpenger.regel.Verneplikt.oppfyllerKravetTilVerneplikt

object KravPåDagpenger {
    val kravPåDagpenger = Opplysningstype.somBoolsk("Krav på dagpenger".tekstId("opplysning.krav-paa-dagpenger"))
    val minsteinntektEllerVerneplikt = Opplysningstype.somBoolsk("Oppfyller kravet til minsteinntekt eller verneplikt")

    val virkningstidspunkt = Opplysningstype.somDato("EttBeregnetVirkningstidspunkt")

    val dagensDato = Opplysningstype.somDato("Dagens dato")

    val regelsett =
        Regelsett("Krav på dagpenger") {
            regel(minsteinntektEllerVerneplikt) { enAv(minsteinntekt, oppfyllerKravetTilVerneplikt) }

            regel(dagensDato) { finnDagensDato }
            regel(virkningstidspunkt) { fraOgMed(kravPåDagpenger) }

            utfall(kravPåDagpenger) {
                alle(
                    Alderskrav.kravTilAlder,
                    FulleYtelser.ikkeFulleYtelser,
                    ReellArbeidssøker.oppyllerKravTilRegistrertArbeidssøker,
                    minsteinntektEllerVerneplikt,
                    Opphold.oppfyllerKravet,
                    ReellArbeidssøker.kravTilArbeidssøker,
                    Rettighetstype.rettighetstype,
                    Samordning.utfallEtterSamordning,
                    StreikOgLockout.ikkeStreikEllerLockout,
                    TapAvArbeidsinntektOgArbeidstid.kravTilTapAvArbeidsinntektOgArbeidstid,
                    Utdanning.kravTilUtdanning,
                    Utestengning.oppfyllerKravetTilIkkeUtestengt,
                )
            }
        }

    val VirkningstidspunktForLangtFramITid =
        Kontrollpunkt(Avklaringspunkter.VirkningstidspunktForLangtFramITid) {
            it.har(virkningstidspunkt) &&
                it.har(dagensDato) &&
                it.finnOpplysning(virkningstidspunkt).verdi.isAfter(
                    it.finnOpplysning(dagensDato).verdi.plusDays(14),
                )
        }
}
