package no.nav.dagpenger

internal open class Vedtak(
    private var omgjortAv: Vedtak?,
) {
    fun omgjøresAv(omgjøringsVedtak: Vedtak) {
        omgjortAv = omgjøringsVedtak
    }
}
