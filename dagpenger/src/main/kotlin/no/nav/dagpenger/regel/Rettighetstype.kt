package no.nav.dagpenger.regel

import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.opplysning.Regelsett
import no.nav.dagpenger.opplysning.RegelsettType
import no.nav.dagpenger.opplysning.id
import no.nav.dagpenger.opplysning.regel.enAv
import no.nav.dagpenger.opplysning.regel.ingenAv
import no.nav.dagpenger.opplysning.regel.innhentes
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
        Regelsett(
            folketrygden.hjemmel(0, 0, "Rettighetstype", "Rettighetstype"),
            RegelsettType.Fastsettelse,
        ) {
            regel(ordinærArbeid) { innhentes }
            regel(permittering) { innhentes }
            regel(lønnsgaranti) { innhentes }
            regel(permitteringFiskeforedling) { innhentes }

            regel(ingenArbeid) { ingenAv(ordinærArbeid, permittering, lønnsgaranti, permitteringFiskeforedling) }
            regel(ordinær) { enAv(ordinærArbeid, ingenArbeid) }

            regel(rettighetstype) { enAv(ordinær, permittering, lønnsgaranti, permitteringFiskeforedling) }
        }
}
