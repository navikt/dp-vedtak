package no.nav.dagpenger.regel

import no.nav.dagpenger.avklaring.Kontrollpunkt
import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.opplysning.Regelsett
import no.nav.dagpenger.opplysning.id
import no.nav.dagpenger.opplysning.regel.erSann
import no.nav.dagpenger.opplysning.regel.ingenAv
import no.nav.dagpenger.opplysning.regel.innhentes
import no.nav.dagpenger.opplysning.regel.oppslag

object FulleYtelser {
    val andreYtelser = Opplysningstype.somBoolsk("Oppgitt andre ytelser utenfor NAV i søknaden".id("OppgittAndreYtelserUtenforNav"))
    val vurderingAndreYtelser = Opplysningstype.somBoolsk("Saksbehandler er enig i at brukeren har andre ytelser")
    val navYtelser = Opplysningstype.somBoolsk("NAV livsoppholdsytelser")

    val ikkeFulleYtelser = Opplysningstype.somBoolsk("Ikke fulle ytelser")

    val regelsett =
        Regelsett("FulleYtelser") {
            regel(andreYtelser) { innhentes }
            regel(vurderingAndreYtelser) { erSann(andreYtelser) }
            regel(navYtelser) { oppslag(Søknadstidspunkt.søknadstidspunkt) { false } }

            regel(ikkeFulleYtelser) {
                ingenAv(vurderingAndreYtelser, navYtelser)
            }
        }

    val AndreYtelserKontrollPunkt =
        Kontrollpunkt(sjekker = Avklaringspunkter.AndreYtelser) { opplysninger ->
            opplysninger.har(andreYtelser) && opplysninger.finnOpplysning(andreYtelser).verdi
        }
}
