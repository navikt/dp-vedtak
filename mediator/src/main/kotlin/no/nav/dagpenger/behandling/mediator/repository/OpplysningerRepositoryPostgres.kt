package no.nav.dagpenger.behandling.mediator.repository

import kotliquery.Row
import kotliquery.Session
import kotliquery.queryOf
import kotliquery.sessionOf
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
import no.nav.dagpenger.opplysning.Saksbehandlerkilde
import no.nav.dagpenger.opplysning.Systemkilde
import no.nav.dagpenger.opplysning.ULID
import no.nav.dagpenger.opplysning.Utledning
import no.nav.dagpenger.opplysning.id
import no.nav.dagpenger.opplysning.verdier.Ulid
import java.time.LocalDateTime
import java.util.UUID

class OpplysningerRepositoryPostgres : OpplysningerRepository {
    override fun hentOpplysninger(opplysningerId: UUID) =
        sessionOf(dataSource).use { session ->
            OpplysningRepository(opplysningerId, session).hentOpplysninger()
        }.let { Opplysninger(opplysningerId, it) }

    override fun lagreOpplysninger(opplysninger: Opplysninger) {
        val unitOfWork = PostgresUnitOfWork.transaction()
        lagreOpplysninger(opplysninger, unitOfWork)
        unitOfWork.commit()
    }

    override fun lagreOpplysninger(
        opplysninger: Opplysninger,
        unitOfWork: UnitOfWork<*>,
    ) = lagreOpplysninger(opplysninger, unitOfWork as PostgresUnitOfWork)

    private fun lagreOpplysninger(
        opplysninger: Opplysninger,
        unitOfWork: PostgresUnitOfWork,
    ) = unitOfWork.inTransaction { tx ->
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

    private class OpplysningRepository(private val opplysningerId: UUID, private val tx: Session) {
        fun hentOpplysninger(): List<Opplysning<*>> {
            val rader: List<OpplysningRad<*>> =
                sessionOf(dataSource).use { session ->
                    session.run(
                        queryOf(
                            //language=PostgreSQL
                            "SELECT * FROM opplysningstabell WHERE opplysninger_id = :id",
                            mapOf("id" to opplysningerId),
                        ).map { row ->
                            val datatype = Datatype.fromString(row.string("datatype"))
                            row.somOpplysningRad(datatype)
                        }.asList,
                    )
                }
            return rader.somOpplysninger()
        }

        private fun <T : Comparable<T>> Row.somOpplysningRad(datatype: Datatype<T>): OpplysningRad<T> {
            val id = uuid("id")
            val opplysningstype = Opplysningstype(string("type_navn").id(string("type_id")), datatype)
            val gyldighetsperiode =
                Gyldighetsperiode(
                    localDateTimeOrNull("gyldig_fom") ?: LocalDateTime.MIN,
                    localDateTimeOrNull("gyldig_tom") ?: LocalDateTime.MAX,
                )
            val status = this.string("status")
            val verdi = datatype.verdi(this)
            val opprettet = this.localDateTime("opprettet")
            val utledetAv = this.stringOrNull("utledet_av")?.let { UtledningRad(it, utledetAv(id)) }

            val kilde = this.uuidOrNull("kilde_id")?.let { kildeId -> hentKilde(kildeId) }

            return OpplysningRad(id, opplysningstype, verdi, status, gyldighetsperiode, utledetAv, kilde, opprettet)
        }

        private fun hentKilde(kildeId: UUID) =
            sessionOf(dataSource).use { session ->
                session.run(
                    queryOf(
                        //language=PostgreSQL
                        """
                        SELECT 
                            opplysning_kilde.id, 
                            opplysning_kilde.type, 
                            opplysning_kilde.opprettet, 
                            opplysning_kilde.registrert, 
                            opplysning_kilde_system.melding_id AS system_melding_id, 
                            opplysning_kilde_saksbehandler.ident AS saksbehandler_ident
                        FROM 
                            opplysning_kilde 
                        LEFT JOIN 
                            opplysning_kilde_system ON opplysning_kilde.id = opplysning_kilde_system.kilde_id
                        LEFT JOIN 
                            opplysning_kilde_saksbehandler ON opplysning_kilde.id = opplysning_kilde_saksbehandler.kilde_id
                        """.trimIndent(),
                        mapOf("id" to kildeId),
                    ).map { row ->
                        val kildeType = row.string("type")
                        val opprettet = row.localDateTime("opprettet")
                        val registrert = row.localDateTime("registrert")
                        when (kildeType) {
                            Systemkilde::class.java.simpleName -> Systemkilde(row.uuid("system_melding_id"), opprettet, kildeId, registrert)
                            Saksbehandlerkilde::class.java.simpleName ->
                                Saksbehandlerkilde(
                                    row.string("saksbehandler_ident"),
                                    opprettet,
                                    kildeId,
                                    registrert,
                                )

                            else -> throw IllegalStateException("Ukjent kilde")
                        }
                    }.asSingle,
                )
            }

        private fun utledetAv(id: UUID): List<UUID> =
            sessionOf(dataSource).use {
                it.run(
                    queryOf(
                        //language=PostgreSQL
                        "SELECT utledet_av FROM opplysning_utledet_av WHERE opplysning_id = :id",
                        mapOf("id" to id),
                    ).map { row ->
                        row.uuid("utledet_av")
                    }.asList,
                )
            }

        @Suppress("UNCHECKED_CAST")
        private fun <T : Comparable<T>> Datatype<T>.verdi(row: Row): T =
            when (this) {
                Boolsk -> row.boolean("verdi_boolsk")
                Dato -> row.localDate("verdi_dato")
                Desimaltall -> row.double("verdi_desimaltall")
                Heltall -> row.int("verdi_heltall")
                ULID -> Ulid(row.string("verdi_string"))
            } as T

        fun lagreOpplysninger(opplysninger: List<Opplysning<*>>) {
            batchOpplysningstyper(opplysninger.map { it.opplysningstype }).run(tx)
            batchOpplysninger(opplysninger).run(tx)
            batchVerdi(opplysninger).run(tx)
            batchOpplysningLink(opplysninger).run(tx)
            lagreUtledetAv(opplysninger)
            lagreKilde(opplysninger)
        }

        private fun lagreKilde(opplysninger: List<Opplysning<*>>) {
            batchKilde(opplysninger).run(tx)
            val kilder = opplysninger.mapNotNull { it.kilde }
            require(kilder.all { it is Systemkilde || it is Saksbehandlerkilde }) { "Mangler lagring av kildetypen" }
            batchKildeSystem(kilder.filterIsInstance<Systemkilde>()).run(tx)
            batchKildeSaksbehandler(kilder.filterIsInstance<Saksbehandlerkilde>()).run(tx)
        }

        private fun batchKilde(opplysninger: List<Opplysning<*>>) =
            BatchStatement(
                // language=PostgreSQL
                """
                INSERT INTO opplysning_kilde (id, opplysning_id, type, opprettet, registrert) 
                VALUES (:id, :opplysningId, :type, :opprettet, :registrert)
                ON CONFLICT DO NOTHING
                """.trimIndent(),
                opplysninger.mapNotNull {
                    it.kilde?.let { kilde ->
                        mapOf(
                            "id" to kilde.id,
                            "opplysningId" to it.id,
                            "type" to kilde.javaClass.simpleName,
                            "opprettet" to kilde.opprettet,
                            "registrert" to kilde.registrert,
                        )
                    }
                },
            )

        private fun batchKildeSystem(kilder: List<Systemkilde>) =
            BatchStatement(
                // language=PostgreSQL
                """
                INSERT INTO opplysning_kilde_system (kilde_id, melding_id) 
                VALUES (:kildeId, :meldingId)
                ON CONFLICT DO NOTHING
                """.trimIndent(),
                kilder.map { kilde ->
                    mapOf(
                        "kildeId" to kilde.id,
                        "meldingId" to kilde.meldingsreferanseId,
                    )
                },
            )

        private fun batchKildeSaksbehandler(kilder: List<Saksbehandlerkilde>) =
            BatchStatement(
                // language=PostgreSQL
                """
                INSERT INTO opplysning_kilde_saksbehandler (kilde_id, ident) 
                VALUES (:kildeId, :ident)
                ON CONFLICT DO NOTHING
                """.trimIndent(),
                kilder.map { kilde ->
                    mapOf(
                        "kildeId" to kilde.id,
                        "ident" to kilde.ident,
                    )
                },
            )

        private fun lagreUtledetAv(opplysninger: List<Opplysning<*>>) {
            val utlededeOpplysninger = opplysninger.filterNot { it.utledetAv == null }
            batchUtledning(utlededeOpplysninger).run(tx)
            utlededeOpplysninger.forEach { opplysning ->
                batchUtledetAv(opplysning).run(tx)
            }
        }

        private fun batchUtledning(opplysninger: List<Opplysning<*>>) =
            BatchStatement(
                // language=PostgreSQL
                """
                INSERT INTO opplysning_utledning (opplysning_id, regel) 
                VALUES (:opplysningId, :regel)
                ON CONFLICT DO NOTHING
                """.trimIndent(),
                opplysninger.mapNotNull {
                    it.utledetAv?.let { utledning ->
                        mapOf(
                            "opplysningId" to it.id,
                            "regel" to utledning.regel,
                        )
                    }
                },
            )

        private fun batchUtledetAv(opplysning: Opplysning<*>) =
            BatchStatement(
                // language=PostgreSQL
                """
                INSERT INTO opplysning_utledet_av (opplysning_id, utledet_av) 
                VALUES (:opplysningId, :utledetAv)
                ON CONFLICT DO NOTHING
                """.trimIndent(),
                opplysning.utledetAv!!.opplysninger.map {
                    mapOf(
                        "opplysningId" to opplysning.id,
                        "utledetAv" to it.id,
                    )
                },
            )

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
                INSERT INTO opplysning (id, status, opplysningstype_id, gyldig_fom, gyldig_tom, opprettet)
                VALUES (:id, :status, (SELECT opplysningstype_id FROM ins), :fom::timestamp, :tom::timestamp, :opprettet)
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
                        "opprettet" to opplysning.opprettet,
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
    }
}

private fun List<OpplysningRad<*>>.somOpplysninger(): List<Opplysning<*>> {
    val opplysningMap = mutableMapOf<UUID, Opplysning<*>>()

    fun <T : Comparable<T>> OpplysningRad<T>.toOpplysning(): Opplysning<*> {
        // If the Opplysning instance has already been created, return it
        opplysningMap[id]?.let { return it }

        // Create the Utledning instance if necessary
        val utledetAv =
            utledetAv?.let { utledetAv ->
                Utledning(
                    utledetAv.regel,
                    utledetAv.opplysninger.mapNotNull { opplysningId ->
                        opplysningMap[opplysningId] ?: this@somOpplysninger.find { it.id == opplysningId }?.toOpplysning()
                    },
                )
            }

        // Create the Opplysning instance
        val opplysning =
            when (status) {
                "Hypotese" -> Hypotese(id, opplysningstype, verdi, gyldighetsperiode, utledetAv, kilde, opprettet)
                "Faktum" -> Faktum(id, opplysningstype, verdi, gyldighetsperiode, utledetAv, kilde, opprettet)
                else -> throw IllegalStateException("Ukjent opplysningstype")
            }

        // Add the Opplysning instance to the map and return it
        opplysningMap[id] = opplysning
        return opplysning
    }

    // Convert all OpplysningRad instances to Opplysning instances
    return this.map { it.toOpplysning() }
}

private data class UtledningRad(
    val regel: String,
    val opplysninger: List<UUID>,
)

private data class OpplysningRad<T : Comparable<T>>(
    val id: UUID,
    val opplysningstype: Opplysningstype<T>,
    val verdi: T,
    val status: String,
    val gyldighetsperiode: Gyldighetsperiode,
    val utledetAv: UtledningRad? = null,
    val kilde: Kilde? = null,
    val opprettet: LocalDateTime,
)
