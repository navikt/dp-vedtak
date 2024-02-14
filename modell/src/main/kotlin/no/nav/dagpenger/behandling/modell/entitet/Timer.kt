package no.nav.dagpenger.behandling.modell.entitet

import kotlin.time.Duration

class Timer(timer: Number) : Comparable<Timer> {
    internal val timer = timer.toDouble()

    init {
        // require(this.timer >= 0) { "Timer må være større enn eller lik 0, er ${this.timer}" }
    }

    internal infix fun prosentAv(nevner: Timer): Prosent =
        Prosent(
            this.timer / nevner.timer * 100,
        )

    operator fun div(number: Int): Timer = Timer(this.timer / number)

    operator fun minus(other: Timer) = Timer(this.timer - other.timer)

    operator fun times(other: Double): Timer = Timer(this.timer * other)

    override fun compareTo(other: Timer): Int = this.timer.compareTo(other.timer)

    override fun equals(other: Any?) = other is Timer && other.timer == this.timer

    override fun toString() = "Timer($timer)"

    companion object {
        val Number.timer get() = Timer(this)
        val Duration.timer: Timer get() = (this.inWholeMinutes.toDouble() / 60).timer

        fun List<Timer>.summer() = this.sumOf { it.timer }.timer
    }

    fun <R> reflection(block: (Double) -> R) = block(timer)
}
