package no.nav.dagpenger.regel.beregning

import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.regel.OpplysningEtellerannet.arbeidsdagId
import no.nav.dagpenger.regel.OpplysningEtellerannet.arbeidstimerId
import no.nav.dagpenger.regel.OpplysningEtellerannet.forbrukId
import no.nav.dagpenger.regel.OpplysningEtellerannet.terskelId

object Beregning {
    val arbeidsdag = Opplysningstype.boolsk(arbeidsdagId, "Arbeidsdag")
    val arbeidstimer = Opplysningstype.heltall(arbeidstimerId, "Arbeidstimer på en arbeidsdag")
    val forbruk = Opplysningstype.boolsk(forbrukId, "Dag som fører til forbruk av dagpengeperiode")

    // TODO: Er dette noe annet enn krav til tap?
    val terskel = Opplysningstype.desimaltall(terskelId, "Terskel for hvor mye arbeid som kan utføres samtidig med dagpenger")
}
