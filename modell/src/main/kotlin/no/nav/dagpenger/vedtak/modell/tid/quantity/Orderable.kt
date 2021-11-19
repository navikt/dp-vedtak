package no.nav.dagpenger.vedtak.modell.tid.quantity

// Understands specific sequences of elements
interface Orderable<T> {
    fun isBetterThan(other: T): Boolean
}

fun <S : Orderable<S>> List<S>.best(): S? {
    if (this.isEmpty()) return null
    return this.reduce { champion, challenger ->
        if (challenger.isBetterThan(champion)) challenger else champion }
}
val <S : Orderable<S>> List<S>.best get() = this.best()