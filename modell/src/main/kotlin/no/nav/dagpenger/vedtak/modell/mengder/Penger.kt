package no.nav.dagpenger.vedtak.modell.mengder

enum class Valuta {
    NOK
}

class Penger(val beløp: Number, val valuta: Valuta = Valuta.NOK)
