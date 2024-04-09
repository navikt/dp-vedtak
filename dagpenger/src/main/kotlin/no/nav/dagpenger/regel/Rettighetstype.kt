package no.nav.dagpenger.regel

import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.opplysning.Regelsett
import no.nav.dagpenger.opplysning.id
import no.nav.dagpenger.opplysning.regel.enAv
import no.nav.dagpenger.opplysning.regel.innhentMed
import no.nav.dagpenger.regel.Behov.Lønnsgaranti
import no.nav.dagpenger.regel.Behov.Ordinær
import no.nav.dagpenger.regel.Behov.Permittert
import no.nav.dagpenger.regel.Behov.PermittertFiskeforedling

object Rettighetstype {
    private val ordinær = Opplysningstype.somBoolsk("Har rett til ordinære dagpenger".id(Ordinær))
    private val permittering = Opplysningstype.somBoolsk("Har rett til dagpenger under permittering".id(Permittert))
    private val lønnsgaranti = Opplysningstype.somBoolsk("Har rett til dagpenger etter konkurs".id(Lønnsgaranti))
    private val permitteringFiskeforedling =
        Opplysningstype.somBoolsk("Har rett til dagpenger under permittering i fiskeforedlingsindustri".id(PermittertFiskeforedling))

    val rettighetstype = Opplysningstype.somBoolsk("Rettighetstype".id("Rettighetstype"))

    val regelsett =
        Regelsett("Rettighetstype") {
            regel(ordinær) { innhentMed() }
            regel(permittering) { innhentMed() }
            regel(lønnsgaranti) { innhentMed() }
            regel(permitteringFiskeforedling) { innhentMed() }
            regel(rettighetstype) { enAv(ordinær, permittering, lønnsgaranti, permitteringFiskeforedling) }
        }
}
