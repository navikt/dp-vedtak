package no.nav.dagpenger.behandling.mediator.melding

import io.kotest.matchers.shouldBe
import no.nav.dagpenger.behandling.db.Postgres.withMigratedDb
import no.nav.dagpenger.opplysning.Faktum
import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.opplysning.verdier.Ulid
import org.junit.jupiter.api.Test
import java.time.LocalDate

class OpplysningRepositoryPostgresTest {
    @Test
    fun `lagre opplysning`() {
        withMigratedDb {
            val repo = OpplysningRepositoryPostgres()
            val opplysninger =
                listOf(
                    Faktum(Opplysningstype.somHeltall("Heltall"), 5),
                    Faktum(Opplysningstype.somDesimaltall("Desimal"), 5.5),
                    Faktum(Opplysningstype.somDato("Dato"), LocalDate.now()),
                    Faktum(Opplysningstype.somBoolsk("Boolsk"), true),
                    Faktum(Opplysningstype.somUlid("Ulid"), Ulid("01E5Z6Z1Z1Z1Z1Z1Z1Z1Z1Z1Z1")),
                )
            val ints =
                opplysninger.map { opplysning ->
                    repo.lagreOpplysning(opplysning)
                }
            ints.sum() shouldBe opplysninger.size
        }
    }
}
