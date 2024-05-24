package no.nav.dagpenger.behandling

import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.opplysning.id

internal object TestOpplysningstyper {
    val baseOpplysningstype = Opplysningstype.somDato("Base")
    val utledetOpplysningstype = Opplysningstype.somHeltall("Utledet")
    val maksdato = Opplysningstype.somDato("MaksDato")
    val mindato = Opplysningstype.somDato("MinDato")
    val heltall = Opplysningstype.somHeltall("heltall")
    val boolsk = Opplysningstype.somBoolsk("boolsk")
    val dato = Opplysningstype.somDato("Dato")
    val desimal = Opplysningstype.somHeltall("Desimal".id("desimaltall"))
}
