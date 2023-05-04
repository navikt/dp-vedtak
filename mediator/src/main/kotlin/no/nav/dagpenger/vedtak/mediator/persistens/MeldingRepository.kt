package no.nav.dagpenger.vedtak.mediator.persistens

interface MeldingRepository {

    fun lagre(hendelseMessage: HendelseMessage)
    fun hentMottatte(): List<HendelseMessage>
    fun hentBehandlede(): List<HendelseMessage>
    fun behandlet(hendelseMessage: HendelseMessage)
    fun hentFeilede(): List<HendelseMessage>
}

interface HendelseMessage {
    fun asJson(): String
    fun eier(): String
    fun meldingId(): String
}
