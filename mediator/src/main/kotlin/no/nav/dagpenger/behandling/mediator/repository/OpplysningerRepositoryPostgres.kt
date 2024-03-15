package no.nav.dagpenger.behandling.mediator.repository

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
import no.nav.dagpenger.opplysning.Opplysninger
import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.opplysning.ULID
import no.nav.dagpenger.opplysning.id
import no.nav.dagpenger.opplysning.verdier.Ulid
import java.time.LocalDateTime
import java.util.UUID

class OpplysningerRepositoryPostgres : OpplysningerRepository {
    override fun hentOpplysninger(opplysningerId: UUID) =
        using(sessionOf(dataSource)) { session ->
            session.transaction { tx ->
                OpplysningRepository(opplysningerId, tx).hentOpplysninger()
            }
        }.let { Opplysninger(it) }

    override fun lagreOpplysninger(opplysninger: Opplysninger) {
        using(sessionOf(dataSource)) { session ->
            session.transaction { tx ->
                tx.run(
                    queryOf(
                        //language=PostgreSQL
                        """
                        INSERT INTO opplysninger (opplysninger_id) VALUES (:opplysningerId) ON CONFLICT DO NOTHING
                        """.trimIndent(),
                        mapOf("opplysningerId" to opplysninger.id),
                    ).asUpdate,
                )
                OpplysningRepository(opplysninger.id, tx).lagreOpplysninger(opplysninger.aktiveOpplysninger())
            }
        }
    }

    override fun lagreOpplysninger(opplysninger: List<Opplysninger>) {
        opplysninger.map { lagreOpplysninger(it) }
    }

    private class OpplysningRepository(private val opplysningerId: UUID, private val tx: TransactionalSession) {
        fun hentOpplysninger(): List<Opplysning<*>> =
            using(sessionOf(dataSource)) { session ->
                session.run(
                    queryOf(
                        //language=PostgreSQL
                        "SELECT * FROM opplysningstabell WHERE opplysninger_id = :id",
                        mapOf("id" to opplysningerId),
                    ).map { row ->
                        val datatype = Datatype.fromString(row.string("datatype"))
                        row.somOpplysning(datatype)
                    }.asList,
                )
            }

        private fun <T : Comparable<T>> Row.somOpplysning(datatype: Datatype<T>): Opplysning<T> {
            val id = uuid("id")
            val opplysningstype = Opplysningstype(string("type_navn").id(string("type_id")), datatype)
            val gyldighetsperiode =
                Gyldighetsperiode(
                    localDateTimeOrNull("gyldig_fom") ?: LocalDateTime.MIN,
                    localDateTimeOrNull("gyldig_tom") ?: LocalDateTime.MAX,
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

        fun lagreOpplysninger(opplysninger: List<Opplysning<*>>) {
            batchOpplysningstyper(opplysninger.map { it.opplysningstype }).run(tx)
            batchOpplysninger(opplysninger).run(tx)
            batchVerdi(opplysninger).run(tx)
            batchOpplysningLink(opplysninger).run(tx)
            // TODO: tx.lagreKilde(opplysning.id, opplysning.kilde)
            // TODO: tx.lagreUtledetAv()
        }

        private fun batchOpplysningLink(opplysninger: List<Opplysning<*>>) =
            BatchStatement(
                """
                INSERT INTO opplysninger_opplysning (opplysninger_id, opplysning_id) 
                VALUES (:opplysningerId, :opplysningId)
                ON CONFLICT DO NOTHING
                """.trimIndent(),
                opplysninger.map {
                    mapOf(
                        "opplysningerId" to opplysningerId,
                        "opplysningId" to it.id,
                    )
                },
            )

        private fun batchOpplysningstyper(opplysningstyper: List<Opplysningstype<*>>) =
            BatchStatement(
                //language=PostgreSQL
                """
                INSERT INTO opplysningstype (id, navn, datatype)
                VALUES (:id, :navn, :datatype)
                ON CONFLICT DO NOTHING 
                """.trimIndent(),
                opplysningstyper.map {
                    mapOf(
                        "id" to it.id,
                        "navn" to it.navn,
                        "datatype" to it.datatype.javaClass.simpleName,
                    )
                },
            )

        private fun batchOpplysninger(opplysninger: List<Opplysning<*>>) =
            BatchStatement(
                //language=PostgreSQL
                """
                WITH ins AS (
                    SELECT opplysningstype_id FROM opplysningstype WHERE id = :typeId AND navn = :typeNavn AND  datatype = :datatype
                )
                INSERT INTO opplysning (id, status, opplysningstype_id, gyldig_fom, gyldig_tom)
                VALUES (:id, :status, (SELECT opplysningstype_id FROM ins), :fom::timestamp, :tom::timestamp)
                ON CONFLICT DO NOTHING
                """.trimIndent(),
                opplysninger.map { opplysning ->
                    val gyldighetsperiode: Gyldighetsperiode = opplysning.gyldighetsperiode
                    mapOf(
                        "id" to opplysning.id,
                        "status" to opplysning.javaClass.simpleName,
                        "typeId" to opplysning.opplysningstype.id,
                        "typeNavn" to opplysning.opplysningstype.navn,
                        "datatype" to opplysning.opplysningstype.datatype.javaClass.simpleName,
                        "fom" to gyldighetsperiode.fom.let { if (it == LocalDateTime.MIN) null else it },
                        "tom" to gyldighetsperiode.tom.let { if (it == LocalDateTime.MAX) null else it },
                    )
                },
            )

        private fun batchVerdi(opplysninger: List<Opplysning<*>>): BatchStatement {
            val defaultVerdi =
                mapOf(
                    "verdi_heltall" to null,
                    "verdi_desimaltall" to null,
                    "verdi_dato" to null,
                    "verdi_boolsk" to null,
                    "verdi_string" to null,
                )
            return BatchStatement(
                //language=PostgreSQL
                """
                INSERT INTO opplysning_verdi (opplysning_id, datatype, verdi_heltall, verdi_desimaltall, verdi_dato, verdi_boolsk, verdi_string) 
                VALUES (:opplysning_id, :datatype, :verdi_heltall, :verdi_desimaltall, :verdi_dato, :verdi_boolsk, :verdi_string)
                ON CONFLICT DO NOTHING
                """.trimIndent(),
                opplysninger.map {
                    val datatype = it.opplysningstype.datatype
                    val (kolonne, data) = verdiKolonne(datatype, it.verdi)
                    val verdi = defaultVerdi + mapOf(kolonne to data)
                    mapOf(
                        "kolonne" to kolonne,
                        "opplysning_id" to it.id,
                        "datatype" to datatype.javaClass.simpleName,
                    ) + verdi
                },
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
    }
}
