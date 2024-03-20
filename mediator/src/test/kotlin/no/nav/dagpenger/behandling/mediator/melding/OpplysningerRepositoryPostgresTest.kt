package no.nav.dagpenger.behandling.mediator.melding

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.longs.shouldBeLessThan
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import no.nav.dagpenger.behandling.db.Postgres.withMigratedDb
import no.nav.dagpenger.behandling.mediator.repository.OpplysningerRepositoryPostgres
import no.nav.dagpenger.opplysning.Faktum
import no.nav.dagpenger.opplysning.Gyldighetsperiode
import no.nav.dagpenger.opplysning.Opplysninger
import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.opplysning.Regelkjøring
import no.nav.dagpenger.opplysning.Regelsett
import no.nav.dagpenger.opplysning.id
import no.nav.dagpenger.opplysning.regel.oppslag
import no.nav.dagpenger.opplysning.verdier.Ulid
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.system.measureTimeMillis

class OpplysningerRepositoryPostgresTest {
    @Test
    fun `lagre opplysning`() {
        withMigratedDb {
            val repo = OpplysningerRepositoryPostgres()
            val datoOpplysningstype = Opplysningstype.somDato("Dato")
            val desimalFaktum =
                Faktum(
                    Opplysningstype.somDesimaltall("Desimal".id("desitall")),
                    5.5,
                    gyldighetsperiode = Gyldighetsperiode(fom = LocalDate.now()),
                )
            val utledetFaktumType = Opplysningstype.somHeltall("Utledet")
            val opplysninger =
                Opplysninger(
                    listOf(
                        desimalFaktum,
                        Faktum(
                            Opplysningstype.somDato("En annen dato"),
                            LocalDate.now(),
                            gyldighetsperiode = Gyldighetsperiode(LocalDate.now(), LocalDate.now()),
                        ),
                        Faktum(Opplysningstype.somBoolsk("Boolsk"), true, gyldighetsperiode = Gyldighetsperiode(tom = LocalDateTime.now())),
                        Faktum(Opplysningstype.somUlid("Ulid"), Ulid("01E5Z6Z1Z1Z1Z1Z1Z1Z1Z1Z1Z1")),
                        // Ulike typer kan bruke samme navn
                        Faktum(Opplysningstype.somBoolsk("Ulid"), false),
                    ),
                )
            val regelkjøring =
                Regelkjøring(
                    LocalDate.now(),
                    opplysninger,
                    Regelsett("Regelsett") { regel(utledetFaktumType) { oppslag(datoOpplysningstype) { 5 } } },
                )
            val datoOpplysning = Faktum(datoOpplysningstype, LocalDate.now())
            opplysninger.leggTil(datoOpplysning)
            repo.lagreOpplysninger(opplysninger).also {
                // Duplikat skriving skal ikke lage duplikate rader
                repo.lagreOpplysninger(opplysninger)
            }

            val inserts = (1..50000).map { Faktum(Opplysningstype.somHeltall("Desimal".id("desitall")), it) }
            val tidBrukt = measureTimeMillis { repo.lagreOpplysninger(Opplysninger(inserts)) }
            tidBrukt shouldBeLessThan 5000

            val fraDb =
                repo.hentOpplysninger(opplysninger.id).also {
                    Regelkjøring(LocalDate.now(), it)
                }

            fraDb.finnAlle().size shouldBe opplysninger.finnAlle().size

            with(fraDb.finnOpplysning(utledetFaktumType)) {
                verdi shouldBe 5
                utledetAv.shouldNotBeNull()
                utledetAv!!.regel shouldBe "Oppslag"
                utledetAv!!.opplysninger shouldContainExactly listOf(datoOpplysning)
            }
            with(fraDb.finnOpplysning(desimalFaktum.id)) {
                id shouldBe desimalFaktum.id
                verdi shouldBe desimalFaktum.verdi
                gyldighetsperiode shouldBe desimalFaktum.gyldighetsperiode
                opplysningstype shouldBe desimalFaktum.opplysningstype
            }
        }
    }
}
