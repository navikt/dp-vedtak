package no.nav.dagpenger

import no.nav.dagpenger.hendelse.ManglendeMeldekortHendelse
import no.nav.dagpenger.hendelse.ProsessResultatHendelse

internal open class Vedtak private constructor(
    private val utfall: Utfall,
    private val tilstand: Tilstand,
    private var omgjortAv: Vedtak?,
    private var endretAv: Vedtak?,
) {
    private constructor(utfall: Utfall, tilstand: Tilstand) : this(utfall, tilstand, null, null)

    companion object {
        fun erAktiv(vedtak: Vedtak) = vedtak.erAktiv()

        fun avslag() = Vedtak(utfall = Utfall.Avslått, tilstand = Tilstand.Avsluttet)
        fun innvilg() = Vedtak(utfall = Utfall.Innvilget, tilstand = Tilstand.Aktiv)
    }

    fun erAktiv(): Boolean {
        endretAv?.let { return it.erAktiv() }
        return tilstand == Tilstand.Aktiv
    }

    fun omgjøresAv(omgjøringsvedtak: Vedtak) {
        omgjortAv = omgjøringsvedtak
    }

    fun endresAv(endringsvedtak: Vedtak) {
        endretAv = endringsvedtak
    }

    fun håndter(hendelse: ProsessResultatHendelse): Boolean {
        tilstand.håndter(this, hendelse)
        return true
    }

    fun håndter(hendelse: ManglendeMeldekortHendelse) {
        endretAv?.let { return it.håndter(hendelse) }
        tilstand.håndter(this, hendelse)
    }

    private interface Tilstand {
        fun håndter(vedtak: Vedtak, hendelse: ProsessResultatHendelse) {}
        fun håndter(vedtak: Vedtak, hendelse: ManglendeMeldekortHendelse) {}

        object Aktiv : Tilstand {
            override fun håndter(vedtak: Vedtak, hendelse: ManglendeMeldekortHendelse) {
                Vedtak(
                    utfall = vedtak.utfall,
                    tilstand = Inaktiv
                ).also {
                    vedtak.endresAv(it)
                }
            }
        }

        object Inaktiv : Tilstand
        object Avsluttet : Tilstand
    }

    internal enum class Utfall {
        Innvilget, Avslått
    }
}
