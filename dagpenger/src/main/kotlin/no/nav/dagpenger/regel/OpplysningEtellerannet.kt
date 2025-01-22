package no.nav.dagpenger.regel

import no.nav.dagpenger.opplysning.BarnDatatype
import no.nav.dagpenger.opplysning.Boolsk
import no.nav.dagpenger.opplysning.Dato
import no.nav.dagpenger.opplysning.Desimaltall
import no.nav.dagpenger.opplysning.Heltall
import no.nav.dagpenger.opplysning.InntektDataType
import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.opplysning.Penger
import no.nav.dagpenger.opplysning.Tekst
import java.util.UUID

object OpplysningEtellerannet {
    val FødselsdatoId = Opplysningstype.Id(UUID.fromString("0194881f-940b-76ff-acf5-ba7bcb367233"), Dato)
    val AldersgrenseId = Opplysningstype.Id(UUID.fromString("0194881f-940b-76ff-acf5-ba7bcb367234"), Heltall)
    val SisteMånedId = Opplysningstype.Id(UUID.fromString("0194881f-940b-76ff-acf5-ba7bcb367235"), Dato)
    val SisteDagIMånedId = Opplysningstype.Id(UUID.fromString("0194881f-940b-76ff-acf5-ba7bcb367236"), Dato)
    val KravTilAlderId = Opplysningstype.Id(UUID.fromString("0194881f-940b-76ff-acf5-ba7bcb367237"), Boolsk)
    val AntallÅrI36MånederId = Opplysningstype.Id(UUID.fromString("0194881f-9410-7481-b263-4606fdd10ca9"), Desimaltall)
    val FaktorForMaksimaltMuligGrunnlagId = Opplysningstype.Id(UUID.fromString("0194881f-9410-7481-b263-4606fdd10ca7"), Desimaltall)
    val SeksGangerGrunnbeløpId = Opplysningstype.Id(UUID.fromString("0194881f-9410-7481-b263-4606fdd10ca8"), Penger)
    val GrunnbeløpForGrunnlagId = Opplysningstype.Id(UUID.fromString("0194881f-940f-7af9-9387-052e028b29ee"), Penger)
    val TellendeInntektId = Opplysningstype.Id(UUID.fromString("0194881f-940f-7af9-9387-052e028b29ed"), InntektDataType)
    val OppjustertInntektId = Opplysningstype.Id(UUID.fromString("0194881f-940f-7af9-9387-052e028b29ec"), InntektDataType)
    val UtbetaltArbeidsinntektPeriode1Id = Opplysningstype.Id(UUID.fromString("0194881f-9410-7481-b263-4606fdd10cad"), Penger)
    val UtbetaltArbeidsinntektPeriode2Id = Opplysningstype.Id(UUID.fromString("0194881f-9410-7481-b263-4606fdd10cae"), Penger)
    val UtbetaltArbeidsinntektPeriode3Id = Opplysningstype.Id(UUID.fromString("0194881f-9410-7481-b263-4606fdd10caf"), Penger)
    val Inntektperiode1Id = Opplysningstype.Id(UUID.fromString("0194881f-9410-7481-b263-4606fdd10cb0"), Penger)
    val Inntektperiode2Id = Opplysningstype.Id(UUID.fromString("0194881f-9410-7481-b263-4606fdd10cb1"), Penger)
    val Inntektperiode3Id = Opplysningstype.Id(UUID.fromString("0194881f-9410-7481-b263-4606fdd10cb2"), Penger)
    val UavkortetGrunnlagSiste12MndId = Opplysningstype.Id(UUID.fromString("0194881f-9410-7481-b263-4606fdd10cbe"), Penger)
    val UavkortetGrunnlagSiste36MndId = Opplysningstype.Id(UUID.fromString("0194881f-9410-7481-b263-4606fdd10cbf"), Penger)
    val AvkortetInntektperiode1Id = Opplysningstype.Id(UUID.fromString("0194881f-9410-7481-b263-4606fdd10cb3"), Penger)
    val AvkortetInntektperiode2Id = Opplysningstype.Id(UUID.fromString("0194881f-9410-7481-b263-4606fdd10cb4"), Penger)
    val AvkortetInntektperiode3Id = Opplysningstype.Id(UUID.fromString("0194881f-9410-7481-b263-4606fdd10cb5"), Penger)
    val GrunnlagSiste12MndId = Opplysningstype.Id(UUID.fromString("0194881f-9410-7481-b263-4606fdd10caa"), Penger)
    val InntektSiste36MånederId = Opplysningstype.Id(UUID.fromString("0194881f-9410-7481-b263-4606fdd10cab"), Penger)
    val GjennomsnittligArbeidsinntektSiste36MånederId = Opplysningstype.Id(UUID.fromString("0194881f-9410-7481-b263-4606fdd10cac"), Penger)
    val UavrundetGrunnlagId = Opplysningstype.Id(UUID.fromString("0194881f-9410-7481-b263-4606fdd10cbb"), Penger)
    val BruktBeregningsregelId = Opplysningstype.Id(UUID.fromString("0194881f-9410-7481-b263-4606fdd10cba"), Tekst)
    val GrunnlagVedOrdinæreDagpengerId = Opplysningstype.Id(UUID.fromString("0194881f-9410-7481-b263-4606fdd10cbc"), Penger)
    val GrunnlagId = Opplysningstype.Id(UUID.fromString("0194881f-9410-7481-b263-4606fdd10cbd"), Penger)
    val HarAvkortetGrunnlagetIPeriode1Id = Opplysningstype.Id(UUID.fromString("0194881f-9410-7481-b263-4606fdd10cb6"), Boolsk)
    val HarAvkortetGrunnlagetIPeriode2Id = Opplysningstype.Id(UUID.fromString("0194881f-9410-7481-b263-4606fdd10cb7"), Boolsk)
    val HarAvkortetGrunnlagetIPeriode3Id = Opplysningstype.Id(UUID.fromString("0194881f-9410-7481-b263-4606fdd10cb8"), Boolsk)
    val HarAvkortetGrunnlagId = Opplysningstype.Id(UUID.fromString("0194881f-9410-7481-b263-4606fdd10cb9"), Boolsk)
    val BarnId = Opplysningstype.Id(UUID.fromString("0194881f-9428-74d5-b160-f63a4c61a23b"), BarnDatatype)
    val AntallBarnSomGirRettTilBarnetilleggId = Opplysningstype.Id(UUID.fromString("0194881f-9428-74d5-b160-f63a4c61a23c"), Heltall)
    val BarnetillegDekningsgradId = Opplysningstype.Id(UUID.fromString("0194881f-9428-74d5-b160-f63a4c61a23e"), Desimaltall)
    val DagsatsUtenBarnetilleggFørSamordningId = Opplysningstype.Id(UUID.fromString("0194881f-9428-74d5-b160-f63a4c61a23f"), Penger)
    val AvrundetDagsatsUtenBarnetilleggId = Opplysningstype.Id(UUID.fromString("0194881f-9428-74d5-b160-f63a4c61a241"), Penger)
    val BarnetilleggetsStørrelsePerDagId =
        Opplysningstype.Id(
            UUID.fromString("0194881f-9428-74d5-b160-f63a4c61a23d"),
            Penger,
        )
    val BarnetilleggId = Opplysningstype.Id(UUID.fromString("0194881f-9428-74d5-b160-f63a4c61a244"), Penger)
    val DagsatsMedBarnetilleggId = Opplysningstype.Id(UUID.fromString("0194881f-9428-74d5-b160-f63a4c61a245"), Penger)
    val AvrundetUkessatsMedBarnetilleggFørSmordningId = Opplysningstype.Id(UUID.fromString("0194881f-9428-74d5-b160-f63a4c61a240"), Penger)
    val NittiProsentId = Opplysningstype.Id(UUID.fromString("0194881f-9428-74d5-b160-f63a4c61a246"), Desimaltall)
    val AntallArbeidsdagerPerÅrId = Opplysningstype.Id(UUID.fromString("0194881f-9428-74d5-b160-f63a4c61a247"), Heltall)
    val MaksGrunnlagId = Opplysningstype.Id(UUID.fromString("0194881f-9428-74d5-b160-f63a4c61a248"), Penger)
    val MaksSatsId = Opplysningstype.Id(UUID.fromString("0194881f-9428-74d5-b160-f63a4c61a24a"), Penger)
    val AvrundetMaksSatsId = Opplysningstype.Id(UUID.fromString("0194881f-9428-74d5-b160-f63a4c61a24b"), Penger)
    val beløpOverMaksId =
        Opplysningstype.Id(
            UUID.fromString(
                "0194881f-9428-74d5-b160-f63a4c61a242",
            ),
            Penger,
        )
    val DagsatsEtterNittiProsentId =
        Opplysningstype.Id(
            UUID.fromString(
                "0194881f-9428-74d5-b160-f63a4c61a243",
            ),
            Penger,
        )
    val SamordnetDagsatsMedBarnetilleggId = Opplysningstype.Id(UUID.fromString("0194881f-9428-74d5-b160-f63a4c61a24d"), Penger)
    val DagsatsEtterSamordningMedBarnetilleggId = Opplysningstype.Id(UUID.fromString("0194881f-9428-74d5-b160-f63a4c61a24f"), Penger)
    val HarSamordnetId = Opplysningstype.Id(UUID.fromString("0194881f-9428-74d5-b160-f63a4c61a250"), Boolsk)
    val ArbeidsdagerPerUkeId = Opplysningstype.Id(UUID.fromString("0194881f-9428-74d5-b160-f63a4c61a249"), Heltall)
    val UkessatsId = Opplysningstype.Id(UUID.fromString("0194881f-9428-74d5-b160-f63a4c61a24e"), Penger)
    val HarBarnetilleggId = Opplysningstype.Id(UUID.fromString("0194881f-9428-74d5-b160-f63a4c61a24c"), Boolsk)
    val KortPeriodeId = Opplysningstype.Id(UUID.fromString("0194881f-943d-77a7-969c-147999f1544a"), Heltall)
    val LangPeriodeId = Opplysningstype.Id(UUID.fromString("0194881f-943d-77a7-969c-147999f1544b"), Heltall)
    val Terskel12Id = Opplysningstype.Id(UUID.fromString("0194881f-943d-77a7-969c-147999f1544c"), Penger)
    val Terskel36Id = Opplysningstype.Id(UUID.fromString("0194881f-943d-77a7-969c-147999f1544d"), Penger)
    val DivisiorId = Opplysningstype.Id(UUID.fromString("0194881f-943d-77a7-969c-147999f1544e"), Desimaltall)
    val TerskelFaktor12Id = Opplysningstype.Id(UUID.fromString("0194881f-943d-77a7-969c-147999f1544f"), Desimaltall)
    val TerskelFaktor36Id = Opplysningstype.Id(UUID.fromString("0194881f-943d-77a7-969c-147999f15450"), Desimaltall)
    val InntektSnittSiste36Id = Opplysningstype.Id(UUID.fromString("0194881f-943d-77a7-969c-147999f15451"), Penger)
    val Overterskel12Id = Opplysningstype.Id(UUID.fromString("0194881f-943d-77a7-969c-147999f15454"), Boolsk)
    val Overterskel36Id = Opplysningstype.Id(UUID.fromString("0194881f-943d-77a7-969c-147999f15455"), Boolsk)
    val Stønadsuker12Id = Opplysningstype.Id(UUID.fromString("0194881f-943d-77a7-969c-147999f15452"), Heltall)
    val Stønadsuker36Id = Opplysningstype.Id(UUID.fromString("0194881f-943d-77a7-969c-147999f15453"), Heltall)
    val AntallStønadsukerId = Opplysningstype.Id(UUID.fromString("0194881f-943d-77a7-969c-147999f15456"), Heltall)
    val IngenOrdinærPeriodeId = Opplysningstype.Id(UUID.fromString("0194881f-943d-77a7-969c-147999f15458"), Heltall)
    val OrdinærPeriodeId = Opplysningstype.Id(UUID.fromString("0194881f-943d-77a7-969c-147999f15459"), Heltall)
    val DagerIUkaId = Opplysningstype.Id(UUID.fromString("0194881f-943d-77a7-969c-147999f15449"), Heltall)
    val GjenståendeStønadsdagerId = Opplysningstype.Id(UUID.fromString("0194881f-943d-77a7-969c-147999f15457"), Heltall)
    val AntallDagsatsForEgenandelId = Opplysningstype.Id(UUID.fromString("0194881f-943f-78d9-b874-00a4944c54f0"), Desimaltall)
    val EgenandelId = Opplysningstype.Id(UUID.fromString("0194881f-943f-78d9-b874-00a4944c54ef"), Penger)
    val IkkeFulleYtelserId = Opplysningstype.Id(UUID.fromString("0194881f-943f-78d9-b874-00a4944c54f1"), Boolsk)
    val MinsteinntektEllerVernepliktId = Opplysningstype.Id(UUID.fromString("0194881f-9440-7e1c-9ec4-0f20650bc0cd"), Boolsk)
    val DagensDatoId = Opplysningstype.Id(UUID.fromString("0194881f-9440-7e1c-9ec4-0f20650bc0cf"), Dato)
    val EttBeregnetVirkningstidspunktId = Opplysningstype.Id(UUID.fromString("0194881f-9440-7e1c-9ec4-0f20650bc0ce"), Dato)
    val KravPåDagpengerId = Opplysningstype.Id(UUID.fromString("0194881f-9440-7e1c-9ec4-0f20650bc0cc"), Boolsk)
    val Maks_lengde_på_opptjeningsperiodeId = UUID.fromString("0194881f-9413-77ce-92ec-d29700f04246")
    val OpptjeningsperiodeFraOgMedId = UUID.fromString("0194881f-9413-77ce-92ec-d29700f04247")
    val InntektId = UUID.fromString("0194881f-9413-77ce-92ec-d29700f04244")
    val Brutto_arbeidsinntektId = UUID.fromString("0194881f-9413-77ce-92ec-d29700f04245")
    val GrunnbeløpId = UUID.fromString("0194881f-9413-77ce-92ec-d29700f04243")
    val InntektSiste12MndId = UUID.fromString("0194881f-9413-77ce-92ec-d29700f04241")
    val Antall_G_for_krav_til_12_mnd_arbeidsinntektId = UUID.fromString("0194881f-9413-77ce-92ec-d29700f0423f")
    val Inntektskrav_for_siste_12_mndId = UUID.fromString("0194881f-9413-77ce-92ec-d29700f04248")
    val Arbeidsinntekt_er_over_kravet_for_siste_12_mndId = UUID.fromString("0194881f-9413-77ce-92ec-d29700f0424a")
    val InntektSiste36MndId = UUID.fromString("0194881f-9413-77ce-92ec-d29700f04242")
    val Antall_G_for_krav_til_36_mnd_arbeidsinntektId = UUID.fromString("0194881f-9413-77ce-92ec-d29700f04240")
    val Inntektskrav_for_siste_36_mndId = UUID.fromString("0194881f-9413-77ce-92ec-d29700f04249")
    val Arbeidsinntekt_er_over_kravet_for_siste_36_mndId = UUID.fromString("0194881f-9413-77ce-92ec-d29700f0424b")
    val Krav_til_minsteinntektId = UUID.fromString("0194881f-9413-77ce-92ec-d29700f0424c")
    val OppholdINorgeId = UUID.fromString("0194881f-9443-72b4-8b30-5f6cdb24d549")
    val Oppfyller_unntak_for_opphold_i_NorgeId = UUID.fromString("0194881f-9443-72b4-8b30-5f6cdb24d54a")
    val Oppfyller_kravet_til_opphold_i_Norge_eller_unntakId = UUID.fromString("0194881f-9443-72b4-8b30-5f6cdb24d54b")
    val Er_personen_medlem_av_folketrygdenId = UUID.fromString("0194881f-9443-72b4-8b30-5f6cdb24d54c")
    val Oppfyller_kravet_til_medlemskapId = UUID.fromString("0194881f-9443-72b4-8b30-5f6cdb24d54d")
    val Oppfyller_kravet_til_opphold_i_NorgeId = UUID.fromString("0194881f-9443-72b4-8b30-5f6cdb24d54e")
    val Lovpålagt_rapporteringsfrist_for_A_ordningenId = UUID.fromString("0194881f-9414-7823-8d29-0e25b7feb7ce")
    val Arbeidsgivers_rapporteringsfristId = UUID.fromString("0194881f-9414-7823-8d29-0e25b7feb7cf")
    val SisteAvsluttendeKalenderMånedId = UUID.fromString("0194881f-9414-7823-8d29-0e25b7feb7d0")
    val KanJobbeDeltidId = UUID.fromString("0194881f-9441-7d1b-a06a-6727543a141e")
    val Det_er_godkjent_at_bruker_kun_søker_deltidsarbeidId = UUID.fromString("0194881f-9441-7d1b-a06a-6727543a141f")
    val KanJobbeHvorSomHelstId = UUID.fromString("0194881f-9442-707b-a6ee-e96c06877bd9")
    val Det_er_godkjent_at_bruker_kun_søk_arbeid_lokaltId = UUID.fromString("0194881f-9442-707b-a6ee-e96c06877bda")
    val HelseTilAlleTyperJobbId = UUID.fromString("0194881f-9442-707b-a6ee-e96c06877bdc")
    val VilligTilÅBytteYrkeId = UUID.fromString("0194881f-9442-707b-a6ee-e96c06877bde")
    val Oppfyller_kravet_til_heltid__og_deltidsarbeidId = UUID.fromString("0194881f-9442-707b-a6ee-e96c06877bd8")
    val Oppfyller_kravet_til_mobilitetId = UUID.fromString("0194881f-9442-707b-a6ee-e96c06877bdb")
    val Oppfyller_kravet_til_å_være_arbeidsførId = UUID.fromString("0194881f-9442-707b-a6ee-e96c06877bdd")
    val Oppfyller_kravet_til_å_ta_ethvert_arbeidId = UUID.fromString("0194881f-9442-707b-a6ee-e96c06877bdf")
    val RegistrertSomArbeidssøkerId = UUID.fromString("0194881f-9442-707b-a6ee-e96c06877be0")
    val Registrert_som_arbeidssøker_på_søknadstidspunktetId = UUID.fromString("0194881f-9442-707b-a6ee-e96c06877be1")
    val Krav_til_arbeidssøkerId = UUID.fromString("0194881f-9442-707b-a6ee-e96c06877be2")
    val OrdinærId = UUID.fromString("0194881f-9444-7a73-a458-0af81c034d85")
    val PermittertId = UUID.fromString("0194881f-9444-7a73-a458-0af81c034d86")
    val LønnsgarantiId = UUID.fromString("0194881f-9444-7a73-a458-0af81c034d87")
    val PermittertFiskeforedlingId = UUID.fromString("0194881f-9444-7a73-a458-0af81c034d88")
    val Har_rett_til_ordinære_dagpenger_uten_arbeidsforholdId = UUID.fromString("0194881f-9444-7a73-a458-0af81c034d8a")
    val Har_rett_til_ordinære_dagpengerId = UUID.fromString("0194881f-9444-7a73-a458-0af81c034d89")
    val RettighetstypeId = UUID.fromString("0194881f-9444-7a73-a458-0af81c034d8b")
    val OppgittAndreYtelserUtenforNavId = UUID.fromString("0194881f-942e-7cb0-aa59-05ea449d88e0")
    val Mottar_pensjon_fra_en_offentlig_tjenestepensjonsordningId = UUID.fromString("0194881f-942e-7cb0-aa59-05ea449d88e1")
    val Mottar_redusert_uførepensjon_fra_offentlig_pensjonsordningId = UUID.fromString("0194881f-942e-7cb0-aa59-05ea449d88e2")
    val Mottar_vartpengerId = UUID.fromString("0194881f-942e-7cb0-aa59-05ea449d88e3")
    val Mottar_ventelønnId = UUID.fromString("0194881f-942e-7cb0-aa59-05ea449d88e4")
    val Mottar_etterlønnId = UUID.fromString("0194881f-942e-7cb0-aa59-05ea449d88e5")
    val Mottar_garantilott_fra_Garantikassen_for_fiskereId = UUID.fromString("0194881f-942e-7cb0-aa59-05ea449d88e6")
    val AndreØkonomiskeYtelserId = UUID.fromString("0194881f-942f-7bde-ab16-68ffd19e9a2a")
    val Pensjon_fra_en_offentlig_tjenestepensjonsordning_beløpId = UUID.fromString("0194881f-942e-7cb0-aa59-05ea449d88e7")
    val Uførepensjon_fra_offentlig_pensjonsordning_beløpId = UUID.fromString("0194881f-942e-7cb0-aa59-05ea449d88e8")
    val Vartpenger_beløpId = UUID.fromString("0194881f-942f-7bde-ab16-68ffd19e9a26")
    val Ventelønn_beløpId = UUID.fromString("0194881f-942f-7bde-ab16-68ffd19e9a27")
    val Etterlønn_beløpId = UUID.fromString("0194881f-942f-7bde-ab16-68ffd19e9a28")
    val Garantilott_fra_Garantikassen_for_fiskere_beløpId = UUID.fromString("0194881f-942f-7bde-ab16-68ffd19e9a29")
    val Sum_av_ytelser_utenfor_folketrygdenId = UUID.fromString("0194881f-942f-7bde-ab16-68ffd19e9a2e")
    val Hvor_mange_prosent_av_G_skal_brukes_som_terskel_ved_samordningId = UUID.fromString("0194881f-942f-7bde-ab16-68ffd19e9a2b")
    val Beløp_tilsvarende_nedre_terskel_av_GId = UUID.fromString("0194881f-942f-7bde-ab16-68ffd19e9a2c")
    val Samordnet_ukessats_uten_barnetilleggId = UUID.fromString("0194881f-942f-7bde-ab16-68ffd19e9a2f")
    val Minste_mulige_ukessats_som_som_kan_brukesId = UUID.fromString("0194881f-942f-7bde-ab16-68ffd19e9a30")
    val Ukessats_trukket_ned_for_ytelser_utenfor_folketrygdenId = UUID.fromString("0194881f-942f-7bde-ab16-68ffd19e9a31")
    val Samordnet_ukessats_med_ytelser_utenfor_folketrygdenId = UUID.fromString("0194881f-942f-7bde-ab16-68ffd19e9a32")
    val Dagsats_uten_barnetillegg_samordnetId = UUID.fromString("0194881f-942f-7bde-ab16-68ffd19e9a33")
    val Skal_samordnes_med_ytelser_utenfor_folketrygdenId = UUID.fromString("0194881f-942f-7bde-ab16-68ffd19e9a2d")
    val SykepengerId = UUID.fromString("0194881f-9433-70e9-a85b-c246150c45cd")
    val PleiepengerId = UUID.fromString("0194881f-9433-70e9-a85b-c246150c45ce")
    val OmsorgspengerId = UUID.fromString("0194881f-9433-70e9-a85b-c246150c45cf")
    val OpplæringspengerId = UUID.fromString("0194881f-9433-70e9-a85b-c246150c45d0")
    val ForeldrepengerId = UUID.fromString("0194881f-9433-70e9-a85b-c246150c45d2")
    val SvangerskapspengerId = UUID.fromString("0194881f-9433-70e9-a85b-c246150c45d3")
    val UføreId = UUID.fromString("0194881f-9433-70e9-a85b-c246150c45d1")
    val Antall_timer_arbeidstiden_skal_samordnes_motId = UUID.fromString("0194881f-9434-79e8-a64d-1a23cc5d86ed")
    val Samordnet_fastsatt_arbeidstidId = UUID.fromString("0194881f-9434-79e8-a64d-1a23cc5d86ee")
    val Sykepenger_dagsatsId = UUID.fromString("0194881f-9433-70e9-a85b-c246150c45d4")
    val Pleiepenger_dagsatsId = UUID.fromString("0194881f-9433-70e9-a85b-c246150c45d5")
    val Omsorgspenger_dagsatsId = UUID.fromString("0194881f-9433-70e9-a85b-c246150c45d6")
    val Opplæringspenger_dagsatsId = UUID.fromString("0194881f-9433-70e9-a85b-c246150c45d7")
    val Uføre_dagsatsId = UUID.fromString("0194881f-9433-70e9-a85b-c246150c45d8")
    val Svangerskapspenger_dagsatsId = UUID.fromString("0194881f-9433-70e9-a85b-c246150c45da")
    val Foreldrepenger_dagsatsId = UUID.fromString("0194881f-9433-70e9-a85b-c246150c45d9")
    val Sum_andre_ytelserId = UUID.fromString("0194881f-9434-79e8-a64d-1a23cc5d86e9")
    val Samordnet_dagsats_uten_barnetilleggId = UUID.fromString("0194881f-9434-79e8-a64d-1a23cc5d86eb")
    val Samordnet_dagsats_er_negativ_eller_0Id = UUID.fromString("0194881f-9434-79e8-a64d-1a23cc5d86ec")
    val Utfall_etter_samordningId = UUID.fromString("0194881f-9434-79e8-a64d-1a23cc5d86ef")
    val Medlem_har_reduserte_ytelser_fra_folketrygden_SamordningId = UUID.fromString("0194881f-9434-79e8-a64d-1a23cc5d86ea")
    val Deltar_medlemmet_i_streik_eller_er_omfattet_av_lock_outId = UUID.fromString("0194881f-91df-746a-a8ac-4a6b2b30685d")
    val Ledig_ved_samme_bedrift_eller_arbeidsplass_og_blir_påvirket_av_utfalletId = UUID.fromString("0194881f-91df-746a-a8ac-4a6b2b30685e")
    val Er_medlemmet_ikke_påvirket_av_streik_eller_lock_outId = UUID.fromString("0194881f-91df-746a-a8ac-4a6b2b30685f")
    val søknadId = Opplysningstype.Id(UUID.fromString("0194881f-91d1-7df2-ba1d-4533f37fcc77"), Tekst)
    val søknadsdatoId = Opplysningstype.Id(UUID.fromString("0194881f-91d1-7df2-ba1d-4533f37fcc73"), Dato)
    val ønskerDagpengerFraDatoId = Opplysningstype.Id(UUID.fromString("0194881f-91d1-7df2-ba1d-4533f37fcc74"), Dato)
    val søknadstidspunktId = Opplysningstype.Id(UUID.fromString("0194881f-91d1-7df2-ba1d-4533f37fcc75"), Dato)
    val prøvingsdatoId = Opplysningstype.Id(UUID.fromString("0194881f-91d1-7df2-ba1d-4533f37fcc76"), Dato)
    val ønsketArbeidstidId = Opplysningstype.Id(UUID.fromString("0194881f-9435-72a8-b1ce-9575cbc2a75f"), Desimaltall)
    val harTaptArbeidId = Opplysningstype.Id(UUID.fromString("0194881f-9435-72a8-b1ce-9575cbc2a75d"), Boolsk)
    val kravPåLønnId = Opplysningstype.Id(UUID.fromString("0194881f-9435-72a8-b1ce-9575cbc2a75e"), Boolsk)
    val ikkeKravPåLønnFraTidligereArbeidsgiverId = Opplysningstype.Id(UUID.fromString("0194881f-9435-72a8-b1ce-9575cbc2a760"), Boolsk)
    val kravTilTapAvArbeidsinntektId = Opplysningstype.Id(UUID.fromString("0194881f-9435-72a8-b1ce-9575cbc2a761"), Boolsk)
    val kravTilProsentvisTapAvArbeidstidId = Opplysningstype.Id(UUID.fromString("0194881f-9435-72a8-b1ce-9575cbc2a762"), Desimaltall)
    val beregningsregelArbeidstidSiste6MånederId = Opplysningstype.Id(UUID.fromString("0194881f-9435-72a8-b1ce-9575cbc2a764"), Boolsk)
    val beregningsregelArbeidstidSiste12MånederId = Opplysningstype.Id(UUID.fromString("0194881f-9435-72a8-b1ce-9575cbc2a765"), Boolsk)
    val beregeningsregelArbeidstidSiste36MånederId = Opplysningstype.Id(UUID.fromString("0194881f-9435-72a8-b1ce-9575cbc2a766"), Boolsk)
    val beregnetVanligArbeidstidPerUkeFørTapId = Opplysningstype.Id(UUID.fromString("0194881f-9435-72a8-b1ce-9575cbc2a767"), Desimaltall)
    val fastsattVanligArbeidstidEtterOrdinærEllerVernepliktId =
        Opplysningstype.Id(
            UUID.fromString("0194881f-9435-72a8-b1ce-9575cbc2a76c"),
            Desimaltall,
        )
    val nyArbeidstidPerUkeId = Opplysningstype.Id(UUID.fromString("0194881f-9435-72a8-b1ce-9575cbc2a76b"), Desimaltall)
    val maksimalVanligArbeidstidId = Opplysningstype.Id(UUID.fromString("0194881f-9435-72a8-b1ce-9575cbc2a768"), Desimaltall)
    val minimumVanligArbeidstidId = Opplysningstype.Id(UUID.fromString("0194881f-9435-72a8-b1ce-9575cbc2a769"), Desimaltall)
    val fastsattArbeidstidPerUkeFørTapId = Opplysningstype.Id(UUID.fromString("0194881f-9435-72a8-b1ce-9575cbc2a76a"), Desimaltall)
    val fastsattVanligArbeidstidErMinstMinimumArbeidstidId =
        Opplysningstype.Id(
            UUID.fromString("0194881f-9435-72a8-b1ce-9575cbc2a76d"),
            Boolsk,
        )
    val tapAvArbeidstidErMinstTerskelId = Opplysningstype.Id(UUID.fromString("0194881f-9435-72a8-b1ce-9575cbc2a76e"), Boolsk)
    val beregningsregelTaptArbeidstidId = Opplysningstype.Id(UUID.fromString("0194881f-9435-72a8-b1ce-9575cbc2a763"), Boolsk)
    val kravTilTapAvArbeidsinntektOgArbeidstidId = Opplysningstype.Id(UUID.fromString("0194881f-9435-72a8-b1ce-9575cbc2a76f"), Boolsk)
    val tarUtdanningEllerOpplæringId = Opplysningstype.Id(UUID.fromString("0194881f-9445-734c-a7ee-045edf29b522"), Boolsk)
    val deltarIArbeidsmarkedstiltakId = Opplysningstype.Id(UUID.fromString("0194881f-9445-734c-a7ee-045edf29b527"), Boolsk)
    val deltarIOpplæringForInnvandrereId = Opplysningstype.Id(UUID.fromString("0194881f-9445-734c-a7ee-045edf29b528"), Boolsk)
    val grunnskoleopplæringId =
        Opplysningstype.Id(
            UUID.fromString(
                "0194881f-9445-734c-a7ee-045edf29b529",
            ),
            Boolsk,
        )
    val deltarIHøyereYrkesfagligUtdanningId = Opplysningstype.Id(UUID.fromString("0194881f-9445-734c-a7ee-045edf29b52a"), Boolsk)
    val deltarIHøyereUtdanningId = Opplysningstype.Id(UUID.fromString("0194881f-9445-734c-a7ee-045edf29b52b"), Boolsk)
    val deltarPåKursMvId = Opplysningstype.Id(UUID.fromString("0194881f-9445-734c-a7ee-045edf29b52c"), Boolsk)
    val godkjentUnntakForUtdanningId = Opplysningstype.Id(UUID.fromString("0194881f-9445-734c-a7ee-045edf29b523"), Boolsk)
    val svartJaPåUtdanningId =
        Opplysningstype.Id(
            UUID.fromString("0194881f-9445-734c-a7ee-045edf29b524"),
            Boolsk,
        )
    val svartNeiPåUtdanningId =
        Opplysningstype.Id(
            UUID.fromString("0194881f-9445-734c-a7ee-045edf29b525"),
            Boolsk,
        )
    val oppfyllerKravetPåUnntakForUtdanningId =
        Opplysningstype.Id(
            UUID.fromString("0194881f-9445-734c-a7ee-045edf29b526"),
            Boolsk,
        )
    val kravTilUtdanningEllerOpplæringId = Opplysningstype.Id(UUID.fromString("0194881f-9445-734c-a7ee-045edf29b52d"), Boolsk)
    val brukerErUtestengtFraDagpengerId = Opplysningstype.Id(UUID.fromString("0194881f-9447-7e36-a569-3e9f42bff9f6"), Boolsk)
    val oppfyllerKravTilIkkeUtestengtId = Opplysningstype.Id(UUID.fromString("0194881f-9447-7e36-a569-3e9f42bff9f7"), Boolsk)
    val VernepliktId = UUID.fromString("0194881f-9423-7e88-918a-85cb7ed76408")
    val Har_utført_minst_tre_måneders_militærtjeneste_eller_obligatorisk_sivilforsvarstjenesteId =
        UUID.fromString(
            "0194881f-9423-7e88-918a-85cb7ed76409",
        )
    val AntallGVernepliktId = Opplysningstype.Id(UUID.fromString("0194881f-9421-766c-9dc6-41fe6c9a1dff"), Desimaltall)
    val VernepliktGrunnlagId = Opplysningstype.Id(UUID.fromString("0194881f-9421-766c-9dc6-41fe6c9a1e00"), Penger)
    val VernepliktPeriodeId = Opplysningstype.Id(UUID.fromString("0194881f-9421-766c-9dc6-41fe6c9a1e01"), Heltall)
    val VernepliktFastsattVanligArbeidstidId = Opplysningstype.Id(UUID.fromString("0194881f-9421-766c-9dc6-41fe6c9a1e02"), Desimaltall)
    val GrunnlagUtenVernepliktId = Opplysningstype.Id(UUID.fromString("0194881f-9421-766c-9dc6-41fe6c9a1e04"), Penger)
    val GrunnlagHvisVernepliktId = Opplysningstype.Id(UUID.fromString("0194881f-9421-766c-9dc6-41fe6c9a1e03"), Penger)
    val GrunnlagForVernepliktErGunstigstId = Opplysningstype.Id(UUID.fromString("0194881f-9421-766c-9dc6-41fe6c9a1e05"), Boolsk)
    val fagsakIdId = UUID.fromString("0194881f-9462-78af-8977-46092bb030eb")
    val avtjentVernepliktId = Opplysningstype.Id(UUID.fromString("01948d3c-4bea-7802-9d18-5342a5e2be99"), Boolsk)
    val oppfyllerKravetTilVernepliktId = Opplysningstype.Id(UUID.fromString("01948d43-e218-76f1-b29b-7e604241d98a"), Boolsk)
}
