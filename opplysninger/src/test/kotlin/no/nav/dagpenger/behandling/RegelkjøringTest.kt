package no.nav.dagpenger.behandling

import no.nav.dagpenger.behandling.regel.enAvRegel
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class RegelkjøringTest {
    @Test
    fun `Regelsett kan ikke inneholder flere regler som produserer samme opplysningstype`() {
        val regelsett1 =
            Regelsett().apply {
                enAvRegel(Opplysningstype("A"), Opplysningstype("B"))
            }
        val regelsett2 =
            Regelsett().apply {
                enAvRegel(Opplysningstype("A"), Opplysningstype("C"))
            }

        assertThrows<IllegalArgumentException> {
            Regelkjøring(1.mai, Opplysninger(), regelsett1, regelsett2)
        }
    }
}
