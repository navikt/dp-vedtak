package no.nav.dagpenger.behandling.modell.entitet

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import no.nav.dagpenger.behandling.modell.entitet.Timer.Companion.timer
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.minutes

class TimerTest {
    @Test
    fun `likhet`() {
        val timer = Timer(2.5)
        timer shouldBe timer
        timer shouldBe Timer(2.5)
        timer shouldBe 150.minutes.timer
        timer shouldNotBe 100.minutes.timer
        timer shouldNotBe Any()
        timer shouldNotBe null
    }
}
