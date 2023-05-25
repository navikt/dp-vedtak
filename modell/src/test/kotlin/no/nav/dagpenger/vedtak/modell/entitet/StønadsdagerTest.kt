package no.nav.dagpenger.vedtak.modell.entitet

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test

class StønadsdagerTest {

    @Test
    fun likhet() {
        Stønadsdager(dager = 10) shouldBe Stønadsdager(dager = 10)
        Dagpengeperiode(52).tilStønadsdager() shouldBe Dagpengeperiode(52).tilStønadsdager()
        Dagpengeperiode(52).tilStønadsdager() shouldBe Stønadsdager(dager = 260)

        Dagpengeperiode(51).tilStønadsdager() shouldNotBe Stønadsdager(dager = 260)
        Dagpengeperiode(51).tilStønadsdager() shouldNotBe Any()
        Dagpengeperiode(51).tilStønadsdager() shouldNotBe null

        Dagpengeperiode(52).tilStønadsdager().hashCode() shouldBe Dagpengeperiode(52).tilStønadsdager().hashCode()
        Dagpengeperiode(52).tilStønadsdager().hashCode() shouldBe Stønadsdager(dager = 260).hashCode()
    }
}
