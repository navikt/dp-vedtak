package no.nav.dagpenger.behandling.mediator.repository

import no.nav.dagpenger.avklaring.Avklaring
import no.nav.dagpenger.behandling.modell.Behandling
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext

class AvklaringRepository(
    private val rapid: MessageContext,
) {
    fun lagre(behandling: Behandling) {
        // TODO: lagre avklaring
        val avklaringer = behandling.aktiveAvklaringer

        /** TODO
         * 1. Lagre avklaring
         * 2. Rehydrere avklaringer
         * 3. Lage AvklaringAvklartHendelse - når en avklaring er avklart
         * 4. Sende AvklaringAvklartHendelse ned modellen til Avklaringer
         * 5. Lage AvklaringAvklartMottak
         * 6. Skrive om dp-manuell-behandling til å lukke avklaringer
         * 7. Fjerne AvklaringManuellBehandling i Behandling og heller sjekke om det er åpne avklaringer
         */

        avklaringer.forEach {
            publiser(
                behandling.behandler.ident,
                behandling.toSpesifikkKontekst(),
                it,
            )
        }
    }

    private fun publiser(
        ident: String,
        kontekst: Behandling.BehandlingKontekst,
        avklaring: Avklaring,
    ) {
        val newMessage =
            JsonMessage.newMessage(
                "NyAvklaring",
                mapOf(
                    "ident" to ident,
                    "avklaringId" to avklaring.id,
                    "kode" to avklaring.kode.kode,
                ) + kontekst.kontekstMap,
            )

        rapid.publish(ident, newMessage.toJson())
    }
}
