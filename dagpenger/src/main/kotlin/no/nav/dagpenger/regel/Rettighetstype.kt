package no.nav.dagpenger.regel

import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.opplysning.Regelsett
import no.nav.dagpenger.opplysning.id
import no.nav.dagpenger.opplysning.regel.enAv
import no.nav.dagpenger.opplysning.regel.ingenAv
import no.nav.dagpenger.opplysning.regel.innhentMed
import no.nav.dagpenger.regel.Behov.Lønnsgaranti
import no.nav.dagpenger.regel.Behov.Ordinær
import no.nav.dagpenger.regel.Behov.Permittert
import no.nav.dagpenger.regel.Behov.PermittertFiskeforedling

object Rettighetstype {
    private val ordinærArbeid = Opplysningstype.somBoolsk("Har rett til ordinære dagpenger gjennom arbeidsforhold".id(Ordinær))
    private val permittering = Opplysningstype.somBoolsk("Har rett til dagpenger under permittering".id(Permittert))
    private val lønnsgaranti = Opplysningstype.somBoolsk("Har rett til dagpenger etter konkurs".id(Lønnsgaranti))
    private val permitteringFiskeforedling =
        Opplysningstype.somBoolsk(
            "Har rett til dagpenger under permittering i fiskeforedlingsindustri".id(PermittertFiskeforedling),
        )

    private val ordinær = Opplysningstype.somBoolsk("Har rett til ordinære dagpenger")
    private val ingenArbeid = Opplysningstype.somBoolsk("Har rett til ordinære dagpenger uten arbeidsforhold")

    val rettighetstype = Opplysningstype.somBoolsk("Rettighetstype".id("Rettighetstype"))

    val regelsett =
        Regelsett("Rettighetstype") {
            regel(ordinærArbeid) { innhentMed() }
            regel(permittering) { innhentMed() }
            regel(lønnsgaranti) { innhentMed() }
            regel(permitteringFiskeforedling) { innhentMed() }

            regel(ingenArbeid) { ingenAv(ordinærArbeid, permittering, lønnsgaranti, permitteringFiskeforedling) }
            regel(ordinær) { enAv(ordinærArbeid, ingenArbeid) }

            regel(rettighetstype) { enAv(ordinær, permittering, lønnsgaranti, permitteringFiskeforedling) }
        }
}
