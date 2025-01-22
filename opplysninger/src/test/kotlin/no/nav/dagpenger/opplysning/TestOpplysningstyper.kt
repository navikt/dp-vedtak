package no.nav.dagpenger.opplysning

import no.nav.dagpenger.opplysning.Opplysningstype.Id
import java.util.UUID

internal object TestOpplysningstyper {
    val dato1 by lazy { Opplysningstype.dato(Id(UUID.fromString("01948e3e-e1fc-7012-896f-dffc3b20eb8a"), Dato), "dato1") }
    val dato2 by lazy { Opplysningstype.dato(Id(UUID.fromString("01948e40-93d7-76db-81be-dd6334a65549"), Dato), "dato2") }
    val desimaltall by lazy {
        Opplysningstype.desimaltall(
            Id(UUID.fromString("01948e40-ccb9-70a9-8f1e-0eb6ce292dfc"), Desimaltall),
            "desimaltall",
        )
    }
    val ulid by lazy { Opplysningstype.ulid(Id(UUID.fromString("01948e42-8b78-77de-8d11-1ac3327e7ae5"), ULID), "ULID") }
    val a by lazy { Opplysningstype.boolsk(Id(UUID.fromString("01948e45-6c8b-701d-962c-3711b67dcb5b"), Boolsk), "A") }
    val b by lazy { Opplysningstype.boolsk(Id(UUID.fromString("01948e45-6c8d-7c75-a07a-e8ded8560902"), Boolsk), "B") }
    val c by lazy { Opplysningstype.boolsk(Id(UUID.fromString("01948e45-6c8d-7c75-a07a-e8ded8560903"), Boolsk), "C") }
    val grunntall by lazy { Opplysningstype.somBeløp("Grunntall") }
    val beløpA by lazy { Opplysningstype.beløp(Id(UUID.fromString("01948e45-6c8d-7c75-a07a-e8ded8560904"), Penger), "BeløpA") }
    val beløpB by lazy { Opplysningstype.beløp(Id(UUID.fromString("01948e45-6c8d-7c75-a07a-e8ded8560905"), Penger), "BeløpB") }
    val faktorA by lazy { Opplysningstype.desimaltall(Id(UUID.fromString("01948e45-6c8d-7c75-a07a-e8ded8560906"), Desimaltall), "FaktorA") }
    val faktorB by lazy { Opplysningstype.desimaltall(Id(UUID.fromString("01948e45-6c8d-7c75-a07a-e8ded8560907"), Desimaltall), "FaktorB") }
    val heltallA by lazy { Opplysningstype.heltall(Id(UUID.fromString("01948e45-6c8d-7c75-a07a-e8ded8560908"), Heltall), "HeltallA") }
    val produkt by lazy {
        Opplysningstype.desimaltall(
            Id(UUID.fromString("01948e45-6c8d-7c75-a07a-e8ded8560909"), Desimaltall),
            "Resultat",
        )
    }
    val boolskB by lazy { Opplysningstype.boolsk(Id(UUID.fromString("01948e47-e063-7eab-9680-a9dcc6d0e921"), Boolsk), "boolsk B") }
    val boolskC by lazy { Opplysningstype.boolsk(Id(UUID.fromString("01948e47-e065-7200-99f3-8649ad150f0a"), Boolsk), "boolsk C") }
    val boolskA by lazy { Opplysningstype.boolsk(Id(UUID.fromString("01948e47-e065-7200-99f3-8649ad150f0b"), Boolsk), "boolsk A") }
    val foreldrevilkår by lazy {
        Opplysningstype.boolsk(
            Id(UUID.fromString("01948e47-e065-7200-99f3-8649ad150f0d"), Boolsk),
            "Foreldrevilkår",
        )
    }
    val undervilkår1 by lazy { Opplysningstype.boolsk(Id(UUID.fromString("01948e47-e065-7200-99f3-8649ad150f0c"), Boolsk), "Undervilkår1") }
    val undervilkår2 by lazy { Opplysningstype.boolsk(Id(UUID.fromString("01948e47-e065-7200-99f3-8649ad150f0e"), Boolsk), "Undervilkår2") }
}
