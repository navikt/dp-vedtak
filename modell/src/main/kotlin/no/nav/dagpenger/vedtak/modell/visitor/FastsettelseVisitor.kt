package no.nav.dagpenger.vedtak.modell.visitor

import no.nav.dagpenger.vedtak.modell.mengde.Tid

internal interface FastsettelseVisitor {

    fun visitForbruk(forbruk: Tid) {}
}
