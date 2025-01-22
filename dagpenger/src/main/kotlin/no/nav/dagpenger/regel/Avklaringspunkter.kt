package no.nav.dagpenger.regel

import no.nav.dagpenger.opplysning.Avklaringkode

object Avklaringspunkter {
    val YtelserUtenforFolketrygden: Avklaringkode =
        Avklaringkode(
            kode = "YtelserUtenforFolketrygden",
            tittel = "Sjekk om det er ytelser utenfor folketrygden som skal samordnes",
            beskrivelse = "Sjekk hvilke ytelser som er oppgitt utenfor folketrygden og om de skal ha konsekvens for dagpengene",
        )

    val FulleYtelser: Avklaringkode =
        Avklaringkode(
            kode = "FulleYtelser",
            tittel = "Sjekk om søker har andre fulle ytelser",
            beskrivelse = "Om søker har andre fulle ytelser må det velges mellom dagpenger eller disse ytelsene",
        )

    val SøknadstidspunktForLangtFramITid =
        Avklaringkode(
            kode = "SøknadstidspunktForLangtFramITid",
            tittel = "Søknadstidspunktet ligger for langt fram i tid",
            beskrivelse = "Søknadstidspunktet ligger mer enn 14 dager fram i tid",
        )

    val VirkningstidspunktForLangtFramITid =
        Avklaringkode(
            kode = "VirkningstidspunktForLangtFramItid",
            tittel = "Virkningstidspunkt ligger for langt fram i tid",
            beskrivelse = "Virkningstidspunkt ligger mer enn 14 dager fram i tid",
        )

    val Verneplikt =
        Avklaringkode(
            kode = "Verneplikt",
            tittel = "Sjekk om søker oppfyller vilkåret til dagpenger ved avtjent verneplikt",
            beskrivelse =
                """
                Søker har oppgitt at de har avtjent verneplikt. Det må sjekkes om kravet til dagpenger ved avtjent verneplikt er oppfylt.
                """.trimIndent(),
        )

    val TapAvArbeidstidBeregningsregel =
        Avklaringkode(
            kode = "TapAvArbeidsinntektOgArbeidstid",
            tittel = "Velg kun en beregningsregel for tap av arbeidsinntekt og arbeidstid",
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
            tittel = "Sjekk om arbeid i EØS fører til sammenlegging",
            beskrivelse = "Personen har oppgitt arbeid fra EØS i søknaden. Det må vurderes om det skal være sammenlegging.",
        )

    val JobbetUtenforNorge =
        Avklaringkode(
            kode = "JobbetUtenforNorge",
            tittel = "Sjekk om arbeid utenfor Norge påvirker retten til dagpenger",
            beskrivelse =
                """
                Personen har oppgitt arbeid utenfor Norge i søknaden. Sjekk om arbeidsforholdene som er oppgitt i søknaden skal være 
                med i vurderingen av retten til dagpenger.
                """.trimIndent(),
        )

    val HattLukkedeSakerSiste8Uker =
        Avklaringkode(
            kode = "HattLukkedeSakerSiste8Uker",
            tittel = "Sjekk om nylig lukkede saker i Arena kan påvirke behandlingen",
            beskrivelse =
                """
                Personen har lukkede saker i Arena siste 8 uker. Har vi nylig gitt avslag bør vi sjekke om det er nødvendig med ekstra 
                veiledning.
                """.trimIndent(),
        )

    val MuligGjenopptak =
        Avklaringkode(
            kode = "MuligGjenopptak",
            tittel = "Sjekk om det er sak som kan gjenopptas i Arena",
            beskrivelse = "Personen har åpne saker i Arena som kan være gjenopptak. Saker som skal gjenopptas må håndteres i Arena.",
        )

    val Samordning =
        Avklaringkode(
            kode = "Samordning",
            tittel = "Sjekk om det er andre ytelser fra folketrygden som skal samordnes",
            beskrivelse = "Sjekk om det er andre ytelser fra folketrygden som skal samordnes.",
        )

    val InntektNesteKalendermåned =
        Avklaringkode(
            kode = "InntektNesteKalendermåned",
            tittel = "Sjekk om inntekt for neste måned er relevant",
            beskrivelse =
                """
                Personen har inntekt som tilhører neste inntektsperiode. Vurder om det er tilstrekkelige inntekter til at utfallet vil 
                endre seg i neste inntektsperiode.
                """.trimIndent(),
        )

    val SvangerskapsrelaterteSykepenger =
        Avklaringkode(
            kode = "SvangerskapsrelaterteSykepenger",
            tittel = "Sjekk om søker har fått sykepenger på grunn av svangerskap som skal med i minsteinntekt",
            beskrivelse =
                """
                Personen har fått utbetalt sykepenger. Om det er svangerskapsrelaterte sykepenger skal være med i inntektstgrunnlaget for 
                vurderingen av minste arbeidsinntekt.
                """.trimIndent(),
        )

    val ØnskerEtterRapporteringsfrist =
        Avklaringkode(
            kode = "ØnskerEtterRapporteringsfrist",
            tittel = "Sjekk om det bør ventes til etter A-ordningens rapporteringsfrist",
            beskrivelse = "Personen har søkt om dagpenger med ønsket startdato i neste rapporteringsperiode.",
        )

    val BrukerUnder18 =
        Avklaringkode(
            kode = "BrukerUnder18",
            tittel = "Bruker er under 18",
            beskrivelse = "Bruker er under 18 og skal ikke ha automatisk behandling",
        )

    val BarnMåGodkjennes =
        Avklaringkode(
            kode = "BarnMåGodkjennes",
            tittel = "Sjekk hvilke barn som skal gi barnetillegg",
            beskrivelse = "Barn må godkjennes om de skal gi barnetillegg",
        )

    val ReellArbeidssøkerUnntak =
        Avklaringkode(
            kode = "ReellArbeidssøkerUnntak",
            tittel = "Sjekk om søker oppfyller unntak til å være reell arbeidssøker",
            beskrivelse = "Det må vurderes om søker kvalifiserer til unntakene til reell arbeidssøker",
        )

    val IkkeRegistrertSomArbeidsøker =
        Avklaringkode(
            kode = "IkkeRegistrertSomArbeidsøker",
            tittel = "Søker er ikke registrert som arbeidssøker",
            beskrivelse = "Søker er ikke registrert som arbeidssøker.",
        )
}
