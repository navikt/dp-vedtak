package no.nav.dagpenger.vedtak.modell

import no.nav.dagpenger.vedtak.kontomodell.TemporalCollection
import no.nav.dagpenger.vedtak.kontomodell.helpers.desember
import no.nav.dagpenger.vedtak.kontomodell.helpers.januar
import no.nav.dagpenger.vedtak.modell.Beløp.Companion.beløp
import no.nav.dagpenger.vedtak.modell.PersonIdentifikator.Companion.tilPersonIdentfikator
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class VedtakFaktaPocTest {
    private val satser = TemporalCollection<Beløp>()
    private val grunnlag = TemporalCollection<Beløp>()
    private val barn = TemporalCollection<VedtakFakta<Barnetillegg>>()
    private val rettighet = TemporalCollection<String>()

    @Test
    fun test() {
        // / hendelse(behandlingsid) -> håndter(hendelse) -> vedtak -> lager vedtakfakta

        // ramme
        grunnlag.put(1.desember, 199323.beløp)
        barn.put(1.desember, VedtakFakta("123", Barnetillegg()))
        satser.put(1.desember, 533.beløp)
        rettighet.put(1.desember, "ORDINÆR")

        assertEquals(0, barn.get(1.januar(2023)).verdi.antall())
        assertEquals(533.beløp, satser.get(1.januar(2023)))
        assertEquals(199323.beløp, grunnlag.get(1.januar(2023)))

        // fått barn
        satser.put(31.desember, 567.beløp)
        barn.put(31.desember, VedtakFakta("456", Barnetillegg(barn = listOf("12345678901".tilPersonIdentfikator()))))

        assertEquals(567.beløp, satser.get(1.januar(2023)))
        assertEquals(1, barn.get(1.januar(2023)).verdi.antall())
        assertEquals(199323.beløp, grunnlag.get(1.januar(2023)))
        assertEquals("ORDINÆR", rettighet.get(1.januar(2023)))
    }

    class VedtakFakta<T>(vedtakId: String, val verdi: T)

    private open class Vilkår(pargraf: String)

    private class Barnetillegg(private val barn: List<PersonIdentifikator> = emptyList()) : Vilkår("§ 4-12 andre ledd") {

        fun antall() = barn.size
    }
}
