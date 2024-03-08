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
import java.math.BigDecimal
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
                ).map { rs ->
                    val status = rs.string("status")
                    val id = rs.uuid("id")
                    val gyldighetsperiode = GyldighetsperiodeDAO(rs.string("fom"), rs.string("tom")).gyldighetsperiode()
                    val datatype1 = rs.string("datatype")
                    val opplysning: Opplysning<*>? =
                        when {
                            datatype1 == "boolsk" -> {
                                val datatype = datatype(datatype1) as Boolsk
                                opplysning(rs, datatype, status, id, gyldighetsperiode)
                            }

                            datatype1 == "Heltall" -> {
                                val datatype = datatype(datatype1) as Heltall
                                opplysning(rs, datatype, status, id, gyldighetsperiode)
                            }

                            else -> {
                                throw IllegalStateException("Ukjent datatype $datatype1")
                            }
                        }
                    opplysning
                }.asSingle,
            )
        }
    }

    private fun opplysning(
        rs: Row,
        datatype: Boolsk,
        status: String,
        id: UUID,
        gyldighetsperiode: Gyldighetsperiode,
    ): Opplysning<*>? {
        val opplysningstype = Opplysningstype(rs.string("opplysningstype"), datatype)
        val verdi =
            verdi(
                datatype,
                rs.boolean("verdi_boolsk"),
                rs.localDateTimeOrNull("verdi_dato"),
                rs.bigDecimalOrNull("verdi_desimaltall"),
                rs.intOrNull("verdi_heltall"),
                rs.stringOrNull("verdi_ulid"),
            )
        return when (status) {
            "Hypotese" -> Hypotese(id, opplysningstype, verdi, gyldighetsperiode)
            "Faktum" -> Faktum(id, opplysningstype, verdi, gyldighetsperiode)
            else -> throw IllegalStateException("Ukjent opplysningstype")
        }
    }

    private fun opplysning(
        rs: Row,
        datatype: Heltall,
        status: String,
        id: UUID,
        gyldighetsperiode: Gyldighetsperiode,
    ): Opplysning<*>? {
        val opplysningstype = Opplysningstype(rs.string("opplysningstype"), datatype)
        val verdi =
            verdi(
                datatype,
                rs.boolean("verdi_boolsk"),
                rs.localDateTimeOrNull("verdi_dato"),
                rs.bigDecimalOrNull("verdi_desimaltall"),
                rs.intOrNull("verdi_heltall"),
                rs.stringOrNull("verdi_ulid"),
            )
        return when (status) {
            "Hypotese" -> Hypotese(id, opplysningstype, verdi, gyldighetsperiode)
            "Faktum" -> Faktum(id, opplysningstype, verdi, gyldighetsperiode)
            else -> throw IllegalStateException("Ukjent opplysningstype")
        }
    }

    private fun datatype(string: String): Datatype<*> =
        when (string) {
            "Boolsk" -> Boolsk
            "Dato" -> Dato
            "Desimaltall" -> Desimaltall
            "Heltall" -> Heltall
            "ULID" -> ULID
            else -> throw IllegalStateException("Ukjent datatype")
        }

    private fun <T : Comparable<T>> verdi(
        datatype: Datatype<T>,
        boolsk: Boolean,
        dato: LocalDateTime?,
        desimaltall: BigDecimal?,
        heltall: Int?,
        ulid: String?,
    ): T =
        when (datatype) {
            Boolsk -> boolsk as T
            Dato -> dato as T
            Desimaltall -> desimaltall as T
            Heltall -> heltall as T
            ULID -> Ulid(ulid as String) as T
        }

    private class GyldighetsperiodeDAO(private val fomString: String, private val tomString: String) {
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
