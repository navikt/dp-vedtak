package no.nav.dagpenger.behandling.mediator.melding

import io.kotest.matchers.shouldBe
import no.nav.dagpenger.behandling.db.Postgres.withMigratedDb
import no.nav.dagpenger.opplysning.Faktum
import no.nav.dagpenger.opplysning.Gyldighetsperiode
import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.opplysning.verdier.Ulid
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime

class OpplysningRepositoryPostgresTest {
    @Test
    fun `lagre opplysning`() {
        withMigratedDb {
            val repo = OpplysningRepositoryPostgres()
            val faktum = Faktum(Opplysningstype.somHeltall("Heltall"), 5)
            val opplysninger =
                listOf(
                    faktum,
                    Faktum(Opplysningstype.somDesimaltall("Desimal"), 5.5, gyldighetsperiode = Gyldighetsperiode(fom = LocalDate.now())),
                    Faktum(
                        Opplysningstype.somDato("Dato"),
                        LocalDate.now(),
                        gyldighetsperiode = Gyldighetsperiode(LocalDate.now(), LocalDate.now()),
                    ),
                    Faktum(Opplysningstype.somBoolsk("Boolsk"), true, gyldighetsperiode = Gyldighetsperiode(tom = LocalDateTime.now())),
                    Faktum(Opplysningstype.somUlid("Ulid"), Ulid("01E5Z6Z1Z1Z1Z1Z1Z1Z1Z1Z1Z1")),
                )
            val ints = opplysninger.map { opplysning -> repo.lagreOpplysning(opplysning) }
            ints.sum() shouldBe opplysninger.size

            with(repo.hentOpplysning(faktum.id)!!) {
                id shouldBe faktum.id
                verdi shouldBe faktum.verdi
                gyldighetsperiode shouldBe faktum.gyldighetsperiode
                opplysningstype shouldBe faktum.opplysningstype
            }
        }
    }
}
