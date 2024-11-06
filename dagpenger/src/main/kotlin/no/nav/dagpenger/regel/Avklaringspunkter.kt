package no.nav.dagpenger.regel

import no.nav.dagpenger.avklaring.Avklaringkode

object Avklaringspunkter {
    val AndreYtelser: Avklaringkode =
        Avklaringkode(
            kode = "AndreYtelser",
            tittel = "Andre ytelser",
            beskrivelse = "Personen har oppgitt andre ytelser",
        )

    val SøknadstidspunktForLangtFramITid =
        Avklaringkode(
            kode = "SøknadstidspunktForLangtFramITid",
            tittel = "Søknadstidspunktet ligger for lang fram i tid",
            beskrivelse = "Søknadstidspunktet ligger mer enn 14 dager fram i tid",
        )

    val VirkningstidspunktForLangtFramITid =
        Avklaringkode(
            kode = "VirkningstidspunktForLangtFramItid",
            tittel = "Virkningstidspunkt ligger for lang fram i tid",
            beskrivelse = "Virkningstidspunkt ligger mer enn 14 dager fram i tid",
        )
    val Verneplikt =
        Avklaringkode(
            kode = "Verneplikt",
            tittel = "Verneplikt",
            beskrivelse = "Krever avklaring om verneplikt",
        )

    val TapAvArbeidstidBeregningsregel =
        Avklaringkode(
            kode = "TapAvArbeidsinntektOgArbeidstid",
            tittel = "Beregningsregel for tap av arbeidsinntekt og arbeidstid",
            kanKvitteres = false,
            beskrivelse =
                """
                Kun én beregningsregel kan være gyldig til en hver tid. 
                Velg en av Arbeidstid siste 6 måneder, Arbeidstid siste 12 måneder eller Arbeidstid siste 36 måneder.
                """.trimIndent(),
        )

    val EØSArbeid =
        Avklaringkode(
            kode = "EØSArbeid",
            tittel = "Arbeid i EØS",
            beskrivelse = "Personen har oppgitt arbeid fra EØS",
        )

    val HattLukkedeSakerSiste8Uker =
        Avklaringkode(
            kode = "HattLukkedeSakerSiste8Uker",
            tittel = "Hatt lukkede saker siste 8 uker",
            beskrivelse = "Personen har lukkede saker i Arena siste 8 uker",
        )

    val InntektNesteKalendermåned =
        Avklaringkode(
            kode = "InntektNesteKalendermåned",
            tittel = "Har innrapport inntekt for neste måned",
            beskrivelse = "Personen har inntekter som tilhører neste inntektsperiode",
        )

    val JobbetUtenforNorge =
        Avklaringkode(
            kode = "JobbetUtenforNorge",
            tittel = "Arbeid utenfor Norge",
            beskrivelse = "Personen har oppgitt arbeid utenfor Norge",
        )

    val MuligGjenopptak =
        Avklaringkode(
            kode = "MuligGjenopptak",
            tittel = "Mulig gjenopptak",
            beskrivelse = "Personen har åpne saker i Arena som kan være gjenopptak",
        )

    val SvangerskapsrelaterteSykepenger =
        Avklaringkode(
            kode = "SvangerskapsrelaterteSykepenger",
            tittel = "Har hatt sykepenger som kan være svangerskapsrelatert",
            beskrivelse = "Personen har sykepenger som kan være svangerskapsrelaterte",
        )

    val ØnskerEtterRapporteringsfrist =
        Avklaringkode(
            kode = "ØnskerEtterRapporteringsfrist",
            tittel = "Ønsker dagpenger etter rapporteringsfrist",
            beskrivelse = "Personen ønsker dagpenger etter rapporteringsfrist",
        )

    val Totrinnskontroll =
        Avklaringkode(
            kode = "Totrinnskontroll",
            tittel = "Totrinnskontroll",
            beskrivelse = "Totrinnskontroll",
            kanKvitteres = true,
        )

    val BrukerUnder18 =
        Avklaringkode(
            kode = "BrukerUnder18",
            tittel = "Bruker er under 18",
            beskrivelse = "Bruker er under 18 og skal ikke ha automatisk behandling",
            kanKvitteres = true,
        )

    val BarnMåGodkjennes =
        Avklaringkode(
            kode = "BarnMåGodkjennes",
            tittel = "Barn må godkjennes for å gi barnetillegg",
            beskrivelse = "Barn må godkjennes om de skal gi barnetillegg",
            kanKvitteres = true,
        )
}
