package no.nav.dagpenger.avklaring

import no.nav.dagpenger.opplysning.Avklaringkode

object TestAvklaringer {
    val ArbeidIEØS =
        Avklaringkode(
            kode = "ArbeidIEØS",
            tittel = "Arbeid i EØS",
            beskrivelse = "Krever avklaring om arbeid i EØS",
            kanKvitteres = true,
        )

    val TestIkke123 =
        Avklaringkode(
            kode = "TestIkke123",
            tittel = "Test må være 123",
            beskrivelse = "Krever avklaring om hvorfor test ikke er 123",
            kanKvitteres = true,
        )

    val SvangerskapsrelaterteSykepenger =
        Avklaringkode(
            kode = "SvangerskapsrelaterteSykepenger",
            tittel = "Sykepenger for svangerskap",
            beskrivelse = "Krever avklaring om hvorfor test ikke er 123",
            kanKvitteres = true,
        )

    val BeregningsregelForFVA =
        Avklaringkode(
            kode = "BeregningsregelForFVA",
            tittel = "BeregningsregelForFVA",
            beskrivelse = "Krever avklaring om hvorfor test ikke er 123",
            kanKvitteres = false,
        )
}
