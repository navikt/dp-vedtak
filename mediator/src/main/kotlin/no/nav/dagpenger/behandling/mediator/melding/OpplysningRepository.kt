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
import no.nav.dagpenger.opplysning.id
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
                    SELECT opplysning.*, opplysning_verdi.*, opplysningstype.id AS type_id, opplysningstype.navn AS type_navn, opplysningstype.datatype
                    FROM opplysning 
                    LEFT JOIN opplysningstype ON opplysning.opplysningstype_id = opplysningstype.opplysningstype_id
                    LEFT JOIN opplysning_verdi ON opplysning.id = opplysning_verdi.opplysning_id
                    WHERE opplysning.id = :id
                    """.trimIndent(),
                    mapOf("id" to opplysningId),
                ).map { row ->
                    val datatype = Datatype.fromString(row.string("datatype"))
                    row.somOpplysning(datatype)
                }.asSingle,
            )
        }
    }

    private fun <T : Comparable<T>> Row.somOpplysning(datatype: Datatype<T>): Opplysning<T> {
        val id = uuid("id")
        val opplysningstype = Opplysningstype(string("type_navn").id(string("type_id")), datatype)
        val gyldighetsperiode =
            Gyldighetsperiode(
                localDateTimeOrNull("fom") ?: LocalDateTime.MIN,
                localDateTimeOrNull("tom") ?: LocalDateTime.MAX,
            )
        val verdi = datatype.verdi(this)
        return when (string("status")) {
            "Hypotese" -> Hypotese(id, opplysningstype, verdi, gyldighetsperiode)
            "Faktum" -> Faktum(id, opplysningstype, verdi, gyldighetsperiode)
            else -> throw IllegalStateException("Ukjent opplysningstype")
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : Comparable<T>> Datatype<T>.verdi(row: Row): T =
        when (this) {
            Boolsk -> row.boolean("verdi_boolsk")
            Dato -> row.localDateTime("verdi_dato")
            Desimaltall -> row.double("verdi_desimaltall")
            Heltall -> row.int("verdi_heltall")
            ULID -> Ulid(row.string("verdi_string"))
        } as T

    override fun lagreOpplysning(opplysning: Opplysning<*>) =
        using(sessionOf(dataSource)) { session ->
            session.transaction { tx ->
                val opplysningId = tx.lagreOpplysningstype(opplysning.opplysningstype)
                tx.lagreOpplysning(opplysning.id, opplysning.javaClass.simpleName, opplysningId, opplysning.gyldighetsperiode)
                // TODO: tx.lagreKilde(opplysning.id, opplysning.kilde)
                // TODO: tx.lagreUtledetAv()
                tx.lagreVerdi(opplysning.id, opplysning.opplysningstype.datatype, opplysning.verdi)
            }
        }

    // TODO: Lage støtte for parent?
    private fun TransactionalSession.lagreOpplysningstype(opplysningstype: Opplysningstype<*>): Long =
        run(
            queryOf(
                //language=PostgreSQL
                """
                WITH ins AS (
                    INSERT INTO opplysningstype (id, navn, datatype)
                    VALUES (:id, :navn, :datatype)
                    ON CONFLICT DO NOTHING 
                    RETURNING opplysningstype_id
                )
                SELECT opplysningstype_id FROM ins
                UNION ALL
                SELECT opplysningstype_id FROM opplysningstype WHERE id = :id AND navn = :navn AND datatype = :datatype
                """.trimIndent(),
                mapOf(
                    "id" to opplysningstype.id,
                    "navn" to opplysningstype.navn,
                    "datatype" to opplysningstype.datatype.javaClass.simpleName,
                ),
            ).map { it.long("opplysningstype_id") }.asSingle,
        ) ?: throw IllegalStateException("Kunne ikke lagre eller finne opplysningstype")

    private fun TransactionalSession.lagreOpplysning(
        id: UUID,
        status: String,
        opplysningstype: Long,
        gyldighetsperiode: Gyldighetsperiode,
    ) = run(
        queryOf(
            //language=PostgreSQL
            """
            INSERT INTO opplysning (id, status, opplysningstype_id, fom, tom)
            VALUES (:id, :status, :opplysningstype, :fom::timestamp, :tom::timestamp)
            ON CONFLICT DO NOTHING
            """.trimIndent(),
            mapOf(
                "id" to id,
                "status" to status,
                "opplysningstype" to opplysningstype,
                "fom" to gyldighetsperiode.fom.let { if (it == LocalDateTime.MIN) null else it },
                "tom" to gyldighetsperiode.tom.let { if (it == LocalDateTime.MAX) null else it },
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
                INSERT INTO opplysning_verdi (opplysning_id, datatype, $kolonne) 
                VALUES (:opplysning_id, :datatype, :verdi)
                ON CONFLICT DO NOTHING
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
        ULID -> Pair("verdi_string", (verdi as Ulid).verdi)
    }
}
