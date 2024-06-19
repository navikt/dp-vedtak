package no.nav.dagpenger.avklaring

enum class TestAvklaringer(
    override val tittel: String,
    override val beskrivelse: String,
    override val kanKvitteres: Boolean = true,
) : Avklaringkode {
    ArbeidIEØS("Arbeid i EØS", "Krever avklaring om arbeid i EØS"),
    TestIkke123("Test må være 123", "Krever avklaring om hvorfor test ikke er 123"),
    SvangerskapsrelaterteSykepenger("Sykepenger for svangerskap", "Krever avklaring om hvorfor test ikke er 123"),
    BeregningsregelForFVA("BeregningsregelForFVA", "Krever avklaring om hvorfor test ikke er 123", false),
}
