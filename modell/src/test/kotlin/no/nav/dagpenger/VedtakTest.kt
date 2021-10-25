package no.nav.dagpenger

import org.junit.jupiter.api.Test

internal class VedtakTest {
    private class Person private constructor(
        private val vedtak: MutableList<Hovedvedtak>
    ) {
        constructor() : this(mutableListOf())

        fun håndter(hendelse: ManglendeMeldekortHendelse) {
            vedtak.forEach {
                it.håndter(hendelse)
            }
        }
    }

    private open class Vedtak(
        private var omgjortAv: Vedtak?,
    ) {
        fun omgjøresAv(omgjøringsVedtak: Vedtak) {
            omgjortAv = omgjøringsVedtak
        }
    }

    private class Hovedvedtak private constructor(
        private var tilstand: Tilstand,
        private val endringer: MutableList<Vedtak>,
        private val sats: Int,
        omgjortAv: Vedtak?
    ) : Vedtak(omgjortAv) {
        constructor() : this(Tilstand.Aktiv, mutableListOf(), 0, null)

        fun håndter(hendelse: ManglendeMeldekortHendelse) {
            tilstand.håndter(this, hendelse)
        }

        private interface Tilstand {
            fun håndter(hovedvedtak: Hovedvedtak, hendelse: ManglendeMeldekortHendelse)

            object Aktiv : Tilstand
            object Inaktiv : Tilstand
            object Avsluttet : Tilstand
        }
    }

    @Test
    fun `Har fått prosessresutalt fra quiz der alt er er oppfylt  innvilgelse`() {
        val person = Person()
        // Ute i verden ett sted (mediator)
        val hendelse = ProsessResultatHendelse(utfall = true)

        person.håndter(hendelse)
    }

    @Test
    fun `Har ikke sendt meldekort`() {
        val person = Person()

        // Ute i verden ett sted (mediator)
        val hendelse = ManglendeMeldekortHendelse()

        person.håndter(hendelse)
    }
}

interface Hendelse

class ProsessResultatHendelse(val utfall: Boolean) : Hendelse
class ManglendeMeldekortHendelse : Hendelse
