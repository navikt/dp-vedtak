package no.nav.dagpenger.vedtak.kontomodell.mengder

enum class Valuta {
    NOK
}

class Penger(val beløp: Number, val valuta: Valuta = Valuta.NOK)
