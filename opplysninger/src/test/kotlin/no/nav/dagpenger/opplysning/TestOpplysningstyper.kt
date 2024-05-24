package no.nav.dagpenger.opplysning

internal object TestOpplysningstyper {
    val dato1 by lazy { Opplysningstype.somDato("dato1") }
    val dato2 by lazy { Opplysningstype.somDato("dato2") }
    val desimaltall by lazy { Opplysningstype.somDesimaltall("desimaltall") }
    val a by lazy { Opplysningstype.somBoolsk("A") }
    val b by lazy { Opplysningstype.somBoolsk("B") }
    val c by lazy { Opplysningstype.somBoolsk("C") }
    val grunntall by lazy { Opplysningstype.somDesimaltall("Grunntall") }
    val faktorA by lazy { Opplysningstype.somDesimaltall("FaktorA") }
    val faktorB by lazy { Opplysningstype.somDesimaltall("FaktorB") }
    val produserer by lazy { Opplysningstype.somDesimaltall("Resultat") }
    val boolskB by lazy { Opplysningstype.somBoolsk("boolsk B") }
    val boolskC by lazy { Opplysningstype.somBoolsk("boolsk C") }
    val boolskA by lazy { Opplysningstype.somBoolsk("boolsk A") }
    val foreldrevilkår by lazy { Opplysningstype.somBoolsk("Foreldrevilkår") }
    val undervilkår1 by lazy { Opplysningstype.somBoolsk("Undervilkår1", foreldrevilkår) }
    val undervilkår2 by lazy { Opplysningstype.somBoolsk("Undervilkår2", foreldrevilkår) }
}
