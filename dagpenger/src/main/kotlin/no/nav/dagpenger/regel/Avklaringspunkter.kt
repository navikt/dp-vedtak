package no.nav.dagpenger.regel

import no.nav.dagpenger.avklaring.Avklaringkode

enum class Avklaringspunkter(
    override val kode: String,
    override val tittel: String,
    override val beskrivelse: String,
    override val kanKvitteres: Boolean = true,
) : Avklaringkode {
    Verneplikt("Verneplikt", "Verneplikt", "Krever avklaring om verneplikt"),
    EØSArbeid("EØSArbeid", "Arbeid i EØS", "Personen har oppgitt arbeid fra EØS"),
    HattLukkedeSakerSiste8Uker(
        "HattLukkedeSakerSiste8Uker",
        "Hatt lukkede saker siste 8 uker",
        "Personen har lukkede saker i Arena siste 8 uker",
    ),
    InntektNesteKalendermåned(
        "InntektNesteKalendermåned",
        "Har innrapport inntekt for neste måned",
        "Personen har inntekter som tilhører neste inntektsperiode",
    ),
    JobbetUtenforNorge("JobbetUtenforNorge", "Arbeid utenfor Norge", "Personen har oppgitt arbeid utenfor Norge"),
    MuligGjenopptak("MuligGjenopptak", "Mulig gjenopptak", "Personen har åpne saker i Arena som kan være gjenopptak"),
    SvangerskapsrelaterteSykepenger(
        "SvangerskapsrelaterteSykepenger",
        "Har hatt sykepenger som kan være svangerskapsrelatert",
        "Personen har sykepenger som kan være svangerskapsrelaterte",
    ),
}
