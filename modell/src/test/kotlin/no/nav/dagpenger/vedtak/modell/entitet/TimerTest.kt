package no.nav.dagpenger.vedtak.modell.entitet

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import no.nav.dagpenger.vedtak.modell.entitet.Timer.Companion.timer
import org.junit.jupiter.api.Test
import java.time.Duration

class TimerTest {
    @Test
    fun `likhet`() {
        val timer = 2.timer
        timer shouldBe 2.timer
        timer.hashCode() shouldBe 2.timer.hashCode()
        timer shouldBe Duration.ofHours(2).timer
        timer shouldNotBe 1.timer
        timer shouldNotBe Duration.ofHours(3)
        timer shouldNotBe Any()
        timer shouldNotBe null
    }
}
