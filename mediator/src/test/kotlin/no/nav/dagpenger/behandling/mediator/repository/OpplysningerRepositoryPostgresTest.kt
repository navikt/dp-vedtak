package no.nav.dagpenger.behandling.mediator.repository

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.longs.shouldBeLessThan
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import no.nav.dagpenger.behandling.TestOpplysningstyper.baseOpplysningstype
import no.nav.dagpenger.behandling.TestOpplysningstyper.beløpA
import no.nav.dagpenger.behandling.TestOpplysningstyper.beløpB
import no.nav.dagpenger.behandling.TestOpplysningstyper.boolsk
import no.nav.dagpenger.behandling.TestOpplysningstyper.dato
import no.nav.dagpenger.behandling.TestOpplysningstyper.desimal
import no.nav.dagpenger.behandling.TestOpplysningstyper.heltall
import no.nav.dagpenger.behandling.TestOpplysningstyper.inntektA
import no.nav.dagpenger.behandling.TestOpplysningstyper.maksdato
import no.nav.dagpenger.behandling.TestOpplysningstyper.mindato
import no.nav.dagpenger.behandling.TestOpplysningstyper.tekst
import no.nav.dagpenger.behandling.TestOpplysningstyper.utledetOpplysningstype
import no.nav.dagpenger.behandling.db.Postgres.withMigratedDb
import no.nav.dagpenger.behandling.mai
import no.nav.dagpenger.behandling.objectMapper
import no.nav.dagpenger.opplysning.Faktum
import no.nav.dagpenger.opplysning.Gyldighetsperiode
import no.nav.dagpenger.opplysning.Opplysning
import no.nav.dagpenger.opplysning.Opplysninger
import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.opplysning.Regelkjøring
import no.nav.dagpenger.opplysning.Regelsett
import no.nav.dagpenger.opplysning.Saksbehandlerkilde
import no.nav.dagpenger.opplysning.regel.oppslag
import no.nav.dagpenger.opplysning.verdier.Beløp
import no.nav.dagpenger.opplysning.verdier.Inntekt
import no.nav.dagpenger.opplysning.verdier.Ulid
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import kotlin.system.measureTimeMillis

class OpplysningerRepositoryPostgresTest {
    @Test
    fun `lagrer enkle opplysninger`() {
        withMigratedDb {
            val repo = OpplysningerRepositoryPostgres()
            val heltallFaktum = Faktum(heltall, 10)
            val kildeA = Saksbehandlerkilde("foo")
            val boolskFaktum = Faktum(boolsk, true, kilde = kildeA)
            val kildeB = Saksbehandlerkilde("bar")
            val datoFaktum = Faktum(dato, LocalDate.now(), kilde = kildeB)
            val desimalltallFaktum = Faktum(desimal, 5.5, kilde = kildeB)
            val tekstFaktum = Faktum(tekst, "Dette er en tekst")

            val opplysninger = Opplysninger(listOf(heltallFaktum, boolskFaktum, datoFaktum, desimalltallFaktum, tekstFaktum))
            repo.lagreOpplysninger(opplysninger)

            val fraDb =
                repo.hentOpplysninger(opplysninger.id).also {
                    Regelkjøring(LocalDate.now(), it)
                }
            fraDb.finnAlle().size shouldBe opplysninger.finnAlle().size
            fraDb.finnOpplysning(heltallFaktum.opplysningstype).verdi shouldBe heltallFaktum.verdi
            fraDb.finnOpplysning(boolskFaktum.opplysningstype).verdi shouldBe boolskFaktum.verdi
            fraDb.finnOpplysning(boolskFaktum.opplysningstype).kilde?.id shouldBe kildeA.id
            fraDb.finnOpplysning(datoFaktum.opplysningstype).verdi shouldBe datoFaktum.verdi
            fraDb.finnOpplysning(datoFaktum.opplysningstype).kilde?.id shouldBe kildeB.id
            fraDb.finnOpplysning(tekstFaktum.opplysningstype).verdi shouldBe tekstFaktum.verdi

            fraDb.finnOpplysning(desimalltallFaktum.opplysningstype).verdi shouldBe desimalltallFaktum.verdi
        }
    }

    @Test
    fun `lagre opplysningens gyldighetsperiode`() {
        withMigratedDb {
            val repo = OpplysningerRepositoryPostgres()
            val gyldighetsperiode1 = Gyldighetsperiode(LocalDate.now(), LocalDate.now().plusDays(14))
            val faktum1 = Faktum(heltall, 10, gyldighetsperiode1)
            val opplysninger = Opplysninger(listOf(faktum1))
            repo.lagreOpplysninger(opplysninger)
            val fraDb =
                repo.hentOpplysninger(opplysninger.id).also {
                    Regelkjøring(LocalDate.now(), it)
                }
            fraDb.finnOpplysning(faktum1.opplysningstype).gyldighetsperiode shouldBe gyldighetsperiode1
        }
    }

    @Test
    fun `lagrer grenseverdier for dato opplysninger`() {
        withMigratedDb {
            val repo = OpplysningerRepositoryPostgres()
            val kilde = Saksbehandlerkilde("foo")
            val maksDatoFaktum = Faktum(maksdato, LocalDate.MAX, kilde = kilde)
            val minDatoFaktum = Faktum(mindato, LocalDate.MIN, kilde = kilde)
            val opplysninger = Opplysninger(listOf(maksDatoFaktum, minDatoFaktum))
            repo.lagreOpplysninger(opplysninger)

            val fraDb =
                repo.hentOpplysninger(opplysninger.id).also {
                    Regelkjøring(LocalDate.now(), it)
                }
            fraDb.finnOpplysning(maksDatoFaktum.opplysningstype).verdi shouldBe maksDatoFaktum.verdi
            fraDb.finnOpplysning(minDatoFaktum.opplysningstype).verdi shouldBe minDatoFaktum.verdi
        }
    }

    @Test
    @Disabled("Modellen støtter ikke å bruke opplysninger med samme navn og ulik type")
    fun `lagrer opplysninger med samme navn og ulik type`() {
        withMigratedDb {
            val repo = OpplysningerRepositoryPostgres()
            val opplysningstype = Opplysningstype.somUlid("Ulid")
            val opplysningstype1 = Opplysningstype.somBoolsk("Ulid")

            val ulidFaktum = Faktum(opplysningstype, Ulid("01E5Z6Z1Z1Z1Z1Z1Z1Z1Z1Z1Z1"))
            val ulidBoolskFaktum = Faktum(opplysningstype1, false)

            val opplysninger = Opplysninger(listOf(ulidFaktum, ulidBoolskFaktum))
            repo.lagreOpplysninger(opplysninger)

            val fraDb =
                repo.hentOpplysninger(opplysninger.id).also {
                    Regelkjøring(LocalDate.now(), it)
                }
            fraDb.finnOpplysning(opplysningstype).verdi shouldBe ulidFaktum.verdi
            fraDb.finnOpplysning(opplysningstype1).verdi shouldBe ulidBoolskFaktum.verdi
        }
    }

    @Test
    fun `lagrer opplysninger med utledning`() {
        withMigratedDb {
            val repo = OpplysningerRepositoryPostgres()

            val baseOpplysning = Faktum(baseOpplysningstype, LocalDate.now())

            val regelsett = Regelsett("Regelsett") { regel(utledetOpplysningstype) { oppslag(baseOpplysningstype) { 5 } } }
            val opplysninger = Opplysninger()
            val regelkjøring = Regelkjøring(LocalDate.now(), opplysninger, regelsett)
            regelkjøring.leggTil(baseOpplysning as Opplysning<*>)

            repo.lagreOpplysninger(opplysninger)

            val fraDb = repo.hentOpplysninger(opplysninger.id).also { Regelkjøring(LocalDate.now(), it) }
            fraDb.finnAlle().size shouldBe opplysninger.finnAlle().size

            with(fraDb.finnOpplysning(utledetOpplysningstype)) {
                verdi shouldBe 5
                utledetAv.shouldNotBeNull()
                utledetAv!!.regel shouldBe "Oppslag"
                utledetAv!!.opplysninger shouldContainExactly listOf(baseOpplysning)
            }
            with(fraDb.finnOpplysning(baseOpplysning.id)) {
                id shouldBe baseOpplysning.id
                verdi shouldBe baseOpplysning.verdi
                gyldighetsperiode shouldBe baseOpplysning.gyldighetsperiode
                opplysningstype shouldBe baseOpplysning.opplysningstype
                utledetAv.shouldBeNull()
            }
        }
    }

    @Test
    fun `Klarer å lagre store mengder opplysninger effektivt`() {
        withMigratedDb {
            val repo = OpplysningerRepositoryPostgres()
            val fakta = (1..50000).map { Faktum(desimal, it.toDouble()) }
            val opplysninger = Opplysninger(fakta)

            val tidBrukt = measureTimeMillis { repo.lagreOpplysninger(opplysninger) }
            tidBrukt shouldBeLessThan 5555

            val fraDb = repo.hentOpplysninger(opplysninger.id)
            fraDb.finnAlle().size shouldBe fakta.size
        }
    }

    @Test
    fun `lagre erstattet opplysning i samme Opplysninger`() {
        withMigratedDb {
            val repo = OpplysningerRepositoryPostgres()
            val opplysning = Faktum(heltall, 10)
            val opplysningErstattet = Faktum(heltall, 20)
            val opplysninger = Opplysninger(listOf(opplysning))
            val regelkjøring = Regelkjøring(LocalDate.now(), opplysninger)

            repo.lagreOpplysninger(opplysninger)
            regelkjøring.leggTil(opplysningErstattet as Opplysning<*>)
            repo.lagreOpplysninger(opplysninger)
            val fraDb =
                repo.hentOpplysninger(opplysninger.id).also {
                    Regelkjøring(LocalDate.now(), it)
                }
            fraDb.aktiveOpplysninger shouldContainExactly opplysninger.aktiveOpplysninger
            fraDb.finnOpplysning(heltall).verdi shouldBe opplysningErstattet.verdi
        }
    }

    @Test
    fun `kan erstatte opplysning i tidligere Opplysninger`() {
        withMigratedDb {
            val repo = OpplysningerRepositoryPostgres()

            // Lag opplysninger med opprinnelig opplysning
            val opplysning = Faktum(heltall, 10)
            val opprinneligOpplysninger = Opplysninger(listOf(opplysning))
            repo.lagreOpplysninger(opprinneligOpplysninger)

            // Lag ny opplysninger med erstattet opplysning
            val opplysningErstattet = Faktum(heltall, 20)
            val erstattetOpplysninger = Opplysninger(opprinneligOpplysninger)
            erstattetOpplysninger.leggTil(opplysningErstattet as Opplysning<*>)
            repo.lagreOpplysninger(erstattetOpplysninger)

            // Verifiser
            val fraDb: Opplysninger =
                // Simulerer hvordan Behandling setter opp Opplysninger
                repo.hentOpplysninger(erstattetOpplysninger.id) + repo.hentOpplysninger(opprinneligOpplysninger.id)

            fraDb.aktiveOpplysninger shouldContainExactly erstattetOpplysninger.aktiveOpplysninger
            fraDb.forDato(10.mai).finnOpplysning(heltall).verdi shouldBe opplysningErstattet.verdi

            // TODO: Noe muffens oppstod i arbeidet rundt erstatning
            fraDb.forDato(10.mai).finnOpplysning(heltall).erstatter shouldBe opplysning
        }
    }

    @Test
    fun `lagrer opplysninger med utledning fra tidligere opplysninger`() {
        withMigratedDb {
            val repo = OpplysningerRepositoryPostgres()

            val baseOpplysning = Faktum(baseOpplysningstype, LocalDate.now())

            val regelsett = Regelsett("Regelsett") { regel(utledetOpplysningstype) { oppslag(baseOpplysningstype) { 5 } } }
            val tidligereOpplysninger = Opplysninger()
            val regelkjøring = Regelkjøring(LocalDate.now(), tidligereOpplysninger, regelsett)

            regelkjøring.leggTil(baseOpplysning as Opplysning<*>)

            repo.lagreOpplysninger(tidligereOpplysninger)

            val nyeOpplysninger = Opplysninger(opplysninger = emptyList(), basertPå = listOf(tidligereOpplysninger))
            val nyRegelkjøring = Regelkjøring(LocalDate.now(), nyeOpplysninger, regelsett)
            val endretBaseOpplysningstype = Faktum(baseOpplysningstype, LocalDate.now().plusDays(1))
            nyRegelkjøring.leggTil(endretBaseOpplysningstype as Opplysning<*>)
            repo.lagreOpplysninger(nyeOpplysninger)

            val fraDb = repo.hentOpplysninger(nyeOpplysninger.id).also { Regelkjøring(LocalDate.now(), it) }
            fraDb.finnAlle().size shouldBe 2

            with(fraDb.finnOpplysning(utledetOpplysningstype)) {
                verdi shouldBe 5
                utledetAv.shouldNotBeNull()
                utledetAv!!.regel shouldBe "Oppslag"
                utledetAv!!.opplysninger shouldContainExactly listOf(endretBaseOpplysningstype)
            }
            with(fraDb.finnOpplysning(endretBaseOpplysningstype.id)) {
                id shouldBe endretBaseOpplysningstype.id
                verdi shouldBe endretBaseOpplysningstype.verdi
                gyldighetsperiode shouldBe endretBaseOpplysningstype.gyldighetsperiode
                opplysningstype shouldBe endretBaseOpplysningstype.opplysningstype
                utledetAv.shouldBeNull()
            }

            val tidligereOpplysningerFraDb = repo.hentOpplysninger(tidligereOpplysninger.id).also { Regelkjøring(LocalDate.now(), it) }
            tidligereOpplysningerFraDb.finnAlle().size shouldBe 2
            with(tidligereOpplysningerFraDb.finnOpplysning(baseOpplysning.id)) {
                id shouldBe baseOpplysning.id
                verdi shouldBe baseOpplysning.verdi
                gyldighetsperiode shouldBe baseOpplysning.gyldighetsperiode
                opplysningstype shouldBe baseOpplysning.opplysningstype
                utledetAv.shouldBeNull()
                erErstattet shouldBe true
                erstattetAv shouldBe listOf(endretBaseOpplysningstype)
            }
            with(tidligereOpplysningerFraDb.finnOpplysning(utledetOpplysningstype)) {
                verdi shouldBe 5
                utledetAv.shouldNotBeNull()
                utledetAv!!.regel shouldBe "Oppslag"
                utledetAv!!.opplysninger shouldContainExactly listOf(baseOpplysning)
            }
        }
    }

    @Test
    fun `lagrer penger som BigDecimal med riktig presisjon`() {
        withMigratedDb {
            val repo = OpplysningerRepositoryPostgres()

            val verdi = "10.00000000000000000006"
            val verdi1 = BigDecimal(verdi)
            val beløpFaktumA = Faktum(beløpA, Beløp(verdi1))
            val beløpFaktumB = Faktum(beløpB, Beløp("EUR 20"))

            val opplysninger = Opplysninger(listOf(beløpFaktumA, beløpFaktumB))
            repo.lagreOpplysninger(opplysninger)

            val fraDb =
                repo.hentOpplysninger(opplysninger.id).also {
                    Regelkjøring(LocalDate.now(), it)
                }

            fraDb.finnAlle().size shouldBe opplysninger.finnAlle().size
            val beløpAFraDB = fraDb.finnOpplysning(beløpFaktumA.opplysningstype)
            beløpAFraDB.verdi shouldBe beløpFaktumA.verdi
            beløpAFraDB.verdi.toString() shouldBe "NOK $verdi"

            val beløpBFraDB = fraDb.finnOpplysning(beløpFaktumB.opplysningstype)
            beløpBFraDB.verdi shouldBe beløpFaktumB.verdi
            beløpBFraDB.verdi.toString() shouldBe "EUR 20"
        }
    }

    @Test
    fun `kan lagre inntekt`() {
        withMigratedDb {
            val repo = OpplysningerRepositoryPostgres()
            val inntektV1: no.nav.dagpenger.inntekt.v1.Inntekt =
                objectMapper.readValue(
                    this.javaClass.getResourceAsStream("/test-data/inntekt.json"),
                    no.nav.dagpenger.inntekt.v1.Inntekt::class.java,
                )
            val inntektFaktum =
                Faktum(
                    inntektA,
                    Inntekt(
                        inntektV1,
                    ),
                )
            val opplysninger = Opplysninger(listOf(inntektFaktum))
            repo.lagreOpplysninger(opplysninger)
            val fraDb = repo.hentOpplysninger(opplysninger.id)

            fraDb.finnOpplysning(inntektA).verdi.id shouldBe inntektFaktum.verdi.id
            fraDb
                .finnOpplysning(inntektA)
                .verdi.verdi.inntektsListe shouldBe inntektFaktum.verdi.verdi.inntektsListe
        }
    }
}
