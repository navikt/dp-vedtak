package no.nav.dagpenger.behandling.mediator.melding

import kotliquery.Row
import kotliquery.TransactionalSession
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.dagpenger.behandling.db.PostgresDataSourceBuilder.dataSource
import no.nav.dagpenger.opplysning.Boolsk
import no.nav.dagpenger.opplysning.Datatype
import no.nav.dagpenger.opplysning.Dato
import no.nav.dagpenger.opplysning.Desimaltall
import no.nav.dagpenger.opplysning.Faktum
import no.nav.dagpenger.opplysning.Gyldighetsperiode
import no.nav.dagpenger.opplysning.Heltall
import no.nav.dagpenger.opplysning.Hypotese
import no.nav.dagpenger.opplysning.Kilde
import no.nav.dagpenger.opplysning.Opplysning
import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.opplysning.ULID
import no.nav.dagpenger.opplysning.Utledning
import no.nav.dagpenger.opplysning.verdier.Ulid
import java.time.LocalDateTime
import java.util.UUID

interface OpplysningRepository {
    fun lagreOpplysning(opplysning: Opplysning<*>): Int

    fun hentOpplysning(opplysningId: UUID): Opplysning<*>?
}

class OpplysningRepositoryPostgres : OpplysningRepository {
    override fun hentOpplysning(opplysningId: UUID): Opplysning<*>? {
        return using(sessionOf(dataSource)) { session ->
            session.run(
                queryOf(
                    //language=PostgreSQL
                    """
                    SELECT o.*, v.datatype, v.verdi_boolsk, v.verdi_dato, v.verdi_desimaltall, v.verdi_heltall, v.verdi_ulid
                    FROM opplysning o
                    LEFT JOIN opplysning_verdi v ON o.id = v.opplysning_id
                    WHERE o.id = :id
                    """.trimIndent(),
                    mapOf("id" to opplysningId),
                ).map { row ->
                    val opplysning: Opplysning<*> =
                        when (val datatype = row.string("datatype")) {
                            "Boolsk" -> row.opplysning(Boolsk)
                            "Heltall" -> row.opplysning(Heltall)
                            "Desimaltall" -> row.opplysning(Desimaltall)
                            else -> throw IllegalStateException("Ukjent datatype $datatype")
                        }
                    opplysning
                }.asSingle,
            )
        }
    }

    private fun <T : Comparable<T>> Row.opplysning(datatype: Datatype<T>): Opplysning<T> {
        val id = uuid("id")
        val opplysningstype = Opplysningstype(string("opplysningstype"), datatype)
        val gyldighetsperiode = GyldighetsperiodeDAO(string("fom"), string("tom")).gyldighetsperiode()
        val verdi = datatype.verdi(this)
        return when (string("status")) {
            "Hypotese" -> Hypotese(id, opplysningstype, verdi, gyldighetsperiode)
            "Faktum" -> Faktum(id, opplysningstype, verdi, gyldighetsperiode)
            else -> throw IllegalStateException("Ukjent opplysningstype")
        }
    }

    private fun <T : Comparable<T>> Datatype<T>.verdi(row: Row): T =
        when (this) {
            Boolsk -> row.boolean("verdi_boolsk") as T
            Dato -> row.localDateTime("verdi_dato") as T
            Desimaltall -> row.bigDecimal("verdi_desimaltall") as T
            Heltall -> row.int("verdi_heltall") as T
            ULID -> Ulid(row.string("ulid")) as T
        }

    private class GyldighetsperiodeDAO(fomString: String, tomString: String) {
        private val fom = infintyToTimestamp(fomString)

        private val tom = infintyToTimestamp(tomString)

        private fun infintyToTimestamp(inf: String) =
            when (inf) {
                "-infinity" -> LocalDateTime.MIN
                "infinity" -> LocalDateTime.MAX
                else -> LocalDateTime.parse(inf)
            }

        fun gyldighetsperiode() = Gyldighetsperiode(fom, tom)
    }

    override fun lagreOpplysning(opplysning: Opplysning<*>) =
        using(sessionOf(dataSource)) { session ->
            session.transaction { tx ->
                tx.lagreOpplysning(opplysning.id, opplysning.javaClass.simpleName, opplysning.opplysningstype, opplysning.gyldighetsperiode)
                // tx.lagreKilde(opplysning.id, opplysning.kilde)
                tx.lagreVerdi(opplysning.id, opplysning.opplysningstype.datatype, opplysning.verdi)
            }
        }

    private fun TransactionalSession.lagreOpplysning(
        id: UUID,
        status: String,
        opplysningstype: Opplysningstype<*>,
        gyldighetsperiode: Gyldighetsperiode,
    ) = run(
        queryOf(
            //language=PostgreSQL
            """
            INSERT INTO opplysning (id, status, opplysningstype, fom, tom)
            VALUES (:id, :status, :opplysningstype, :fom::timestamp, :tom::timestamp)
            """.trimIndent(),
            mapOf(
                "id" to id,
                "status" to status,
                "opplysningstype" to opplysningstype.navn,
                "fom" to gyldighetsperiode.fom.let { if (it == LocalDateTime.MIN) "-infinity" else it },
                "tom" to gyldighetsperiode.tom.let { if (it == LocalDateTime.MAX) "infinity" else it },
            ),
        ).asUpdate,
    )

    private fun TransactionalSession.lagreKilde(
        opplysningId: UUID,
        kilde: Kilde?,
    ) = run(
        queryOf(
            //language=PostgreSQL
            """INSERT INTO opplysning_kilde (opplysning_id, meldingsreferanse_id) VALUES (?,?)""",
            mapOf(
                "opplysning_id" to opplysningId,
                "meldingsreferanse_id" to kilde?.meldingsreferanseId,
            ),
        ).asUpdate,
    )

    private fun TransactionalSession.lagreVerdi(
        opplysningId: UUID,
        datatype: Datatype<*>,
        verdi: Any,
    ): Int {
        val (kolonne, data) = verdiKolonne(datatype, verdi)
        return run(
            queryOf(
                //language=PostgreSQL
                """
                INSERT INTO opplysning_verdi (opplysning_id, datatype, $kolonne) VALUES (:opplysning_id, :datatype, :verdi)
                """.trimIndent(),
                mapOf(
                    "opplysning_id" to opplysningId,
                    "datatype" to datatype.javaClass.simpleName,
                    "verdi" to data,
                ),
            ).asUpdate,
        )
    }

    private fun verdiKolonne(
        datatype: Datatype<*>,
        verdi: Any,
    ) = when (datatype) {
        Boolsk -> Pair("verdi_boolsk", verdi)
        Dato -> Pair("verdi_dato", verdi)
        Desimaltall -> Pair("verdi_desimaltall", verdi)
        Heltall -> Pair("verdi_heltall", verdi)
        ULID -> Pair("verdi_ulid", (verdi as Ulid).verdi)
    }

    private data class Memento<T : Comparable<T>>(
        private val type: Class<out Opplysning<T>>,
        private val id: UUID,
        private val opplysningstype: Opplysningstype<T>,
        private val verdi: T,
        private val gyldighetsperiode: Gyldighetsperiode,
        private val utledetAv: Utledning?,
        private val kilde: Kilde?,
    ) {
        fun restore(): Opplysning<T> {
            return when (type) {
                Hypotese::class.java -> Hypotese(id, opplysningstype, verdi, gyldighetsperiode, utledetAv, kilde)
                Faktum::class.java -> Faktum(id, opplysningstype, verdi, gyldighetsperiode, utledetAv, kilde)
                else -> throw IllegalStateException("Ukjent opplysningstype")
            }
        }
    }
}
