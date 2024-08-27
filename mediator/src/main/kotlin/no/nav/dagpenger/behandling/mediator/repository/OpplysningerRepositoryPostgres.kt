package no.nav.dagpenger.behandling.mediator.repository

import kotliquery.Row
import kotliquery.Session
import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.dagpenger.behandling.db.PostgresDataSourceBuilder.dataSource
import no.nav.dagpenger.behandling.objectMapper
import no.nav.dagpenger.opplysning.Boolsk
import no.nav.dagpenger.opplysning.Datatype
import no.nav.dagpenger.opplysning.Dato
import no.nav.dagpenger.opplysning.Desimaltall
import no.nav.dagpenger.opplysning.Faktum
import no.nav.dagpenger.opplysning.Gyldighetsperiode
import no.nav.dagpenger.opplysning.Heltall
import no.nav.dagpenger.opplysning.Hypotese
import no.nav.dagpenger.opplysning.InntektDataType
import no.nav.dagpenger.opplysning.Kilde
import no.nav.dagpenger.opplysning.Opplysning
import no.nav.dagpenger.opplysning.Opplysninger
import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.opplysning.Penger
import no.nav.dagpenger.opplysning.Tekst
import no.nav.dagpenger.opplysning.ULID
import no.nav.dagpenger.opplysning.Utledning
import no.nav.dagpenger.opplysning.id
import no.nav.dagpenger.opplysning.verdier.Beløp
import no.nav.dagpenger.opplysning.verdier.Inntekt
import no.nav.dagpenger.opplysning.verdier.Ulid
import org.postgresql.util.PGobject
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class OpplysningerRepositoryPostgres : OpplysningerRepository {
    override fun hentOpplysninger(opplysningerId: UUID) =
        sessionOf(dataSource)
            .use { session ->
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
        OpplysningRepository(opplysninger.id, tx).lagreOpplysninger(opplysninger.aktiveOpplysninger)
    }

    private class OpplysningRepository(
        private val opplysningerId: UUID,
        private val tx: Session,
        private val kildeRespository: KildeRepository = KildeRepository(),
    ) {
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

            val kilder = kildeRespository.hentKilder(rader.mapNotNull { it.kildeId })
            val erstattetAv =
                rader
                    .map {
                        it.erstattetAv.filterNot(eksisterer(rader)).mapNotNull { uuid -> hentOpplysning(uuid) }
                    }.flatten()
            val raderMedKilde =
                rader.map {
                    if (it.kildeId == null) return@map it
                    val kilde = kilder[it.kildeId] ?: throw IllegalStateException("Mangler kilde")
                    it.copy(kilde = kilde)
                }

            val raderFraTidligereOpplysninger = rader.filterNot { it.opplysingerId == opplysningerId }.map { it.id }
            return (raderMedKilde + erstattetAv).somOpplysninger().filterNot { it.id in raderFraTidligereOpplysninger }
        }

        private fun eksisterer(rader: List<OpplysningRad<*>>) =
            { opplysningId: UUID ->
                rader.any { annen -> annen.id == opplysningId }
            }

        private fun hentOpplysning(id: UUID): OpplysningRad<*>? {
            val rader: OpplysningRad<*>? =
                sessionOf(dataSource).use { session ->
                    session.run(
                        queryOf(
                            //language=PostgreSQL
                            "SELECT * FROM opplysningstabell WHERE id = :id",
                            mapOf("id" to id),
                        ).map { row ->
                            val datatype = Datatype.fromString(row.string("datatype"))
                            row.somOpplysningRad(datatype)
                        }.asSingle,
                    )
                }
            return rader
        }

        private fun <T : Comparable<T>> Row.somOpplysningRad(datatype: Datatype<T>): OpplysningRad<T> {
            val opplysingerId = uuid("opplysninger_id")
            val id = uuid("id")

            val opplysningstype: Opplysningstype<T> =
                Opplysningstype(string("type_navn").id(string("type_id"), stringOrNull("tekst_id")), datatype)

            val gyldighetsperiode =
                Gyldighetsperiode(
                    localDateOrNull("gyldig_fom") ?: LocalDate.MIN,
                    localDateOrNull("gyldig_tom") ?: LocalDate.MAX,
                )
            val status = this.string("status")
            val verdi = datatype.verdi(this)
            val opprettet = this.localDateTime("opprettet")
            val utledetAvId = this.arrayOrNull<UUID>("utledet_av_id")?.toList() ?: emptyList()
            val utledetAv = this.stringOrNull("utledet_av")?.let { UtledningRad(it, utledetAvId) }
            val erstattetAvId = this.arrayOrNull<UUID>("erstattet_av_id")?.toList() ?: emptyList()

            val kildeId = this.uuidOrNull("kilde_id")

            return OpplysningRad(
                opplysingerId = opplysingerId,
                id = id,
                opplysningstype = opplysningstype,
                verdi = verdi,
                status = status,
                gyldighetsperiode = gyldighetsperiode,
                utledetAv = utledetAv,
                kildeId = kildeId,
                kilde = null,
                opprettet = opprettet,
                erstattetAv = erstattetAvId,
            )
        }

        @Suppress("UNCHECKED_CAST")
        private fun <T : Comparable<T>> Datatype<T>.verdi(row: Row): T =
            when (this) {
                Boolsk -> row.boolean("verdi_boolsk")
                Dato ->
                    when (row.string("verdi_dato")) {
                        "-infinity" -> LocalDate.MIN
                        "infinity" -> LocalDate.MAX
                        else -> row.localDate("verdi_dato")
                    }

                Desimaltall -> row.double("verdi_desimaltall")
                Heltall -> row.int("verdi_heltall")
                ULID -> Ulid(row.string("verdi_string"))
                Penger -> Beløp(row.string("verdi_string"))
                InntektDataType ->
                    Inntekt(
                        row.binaryStream("verdi_jsonb").use {
                            objectMapper.readValue(it, no.nav.dagpenger.inntekt.v1.Inntekt::class.java)
                        },
                    )

                Tekst -> row.string("verdi_string")
            } as T

        fun lagreOpplysninger(opplysninger: List<Opplysning<*>>) {
            kildeRespository.lagreKilder(opplysninger.mapNotNull { it.kilde }, tx)
            batchOpplysningstyper(opplysninger.map { it.opplysningstype }).run(tx)
            batchOpplysninger(opplysninger).run(tx)
            lagreErstattetAv(opplysninger).run(tx)
            batchVerdi(opplysninger).run(tx)
            batchOpplysningLink(opplysninger).run(tx)
            lagreUtledetAv(opplysninger)
        }

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
                opplysninger.mapNotNull { opplysningSomBleUtledet ->
                    opplysningSomBleUtledet.utledetAv?.let { utledning ->
                        mapOf(
                            "opplysningId" to opplysningSomBleUtledet.id,
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
                INSERT INTO opplysningstype (id, navn, tekst_id, datatype)
                VALUES (:id, :navn, :tekstId, :datatype)
                ON CONFLICT DO NOTHING 
                """.trimIndent(),
                opplysningstyper.map {
                    mapOf(
                        "id" to it.id,
                        "navn" to it.navn,
                        "tekstId" to it.tekstId,
                        "datatype" to it.datatype.navn(),
                    )
                },
            )

        private fun batchOpplysninger(opplysninger: List<Opplysning<*>>) =
            BatchStatement(
                //language=PostgreSQL
                """
                WITH ins AS (
                    SELECT opplysningstype_id FROM opplysningstype WHERE id = :typeId AND navn = :typeNavn AND datatype = :datatype 
                )
                INSERT INTO opplysning (id, status, opplysningstype_id, kilde_id, gyldig_fom, gyldig_tom, opprettet)
                VALUES (:id, :status, (SELECT opplysningstype_id FROM ins), :kilde_id, :fom::timestamp, :tom::timestamp, :opprettet)
                ON CONFLICT DO NOTHING
                """.trimIndent(),
                opplysninger.map { opplysning ->
                    val gyldighetsperiode: Gyldighetsperiode = opplysning.gyldighetsperiode
                    mapOf(
                        "id" to opplysning.id,
                        "status" to opplysning.javaClass.simpleName,
                        "typeId" to opplysning.opplysningstype.id,
                        "typeNavn" to opplysning.opplysningstype.navn,
                        "datatype" to opplysning.opplysningstype.datatype.navn(),
                        "kilde_id" to opplysning.kilde?.id,
                        "fom" to gyldighetsperiode.fom.let { if (it == LocalDate.MIN) null else it },
                        "tom" to gyldighetsperiode.tom.let { if (it == LocalDate.MAX) null else it },
                        "opprettet" to opplysning.opprettet,
                    )
                },
            )

        private fun lagreErstattetAv(opplysninger: List<Opplysning<*>>) =
            BatchStatement(
                //language=PostgreSQL
                """
                INSERT INTO opplysning_erstattet_av (opplysning_id, erstattet_av) 
                VALUES (:opplysning_id, :erstattet_av)
                ON CONFLICT DO NOTHING
                """.trimIndent(),
                opplysninger.mapNotNull { opplysning ->
                    if (opplysning.erstatter == null) return@mapNotNull null
                    mapOf(
                        "opplysning_id" to opplysning.erstatter!!.id,
                        "erstattet_av" to opplysning.id,
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
                INSERT INTO opplysning_verdi (opplysning_id, datatype, verdi_heltall, verdi_desimaltall, verdi_dato, verdi_boolsk, verdi_string, verdi_jsonb) 
                VALUES (:opplysning_id, :datatype, :verdi_heltall, :verdi_desimaltall, :verdi_dato, :verdi_boolsk, :verdi_string, :verdi_jsonb)
                ON CONFLICT DO NOTHING
                """.trimIndent(),
                opplysninger.map {
                    val datatype = it.opplysningstype.datatype
                    val (kolonne, data) = verdiKolonne(datatype, it.verdi)
                    val verdi = defaultVerdi + mapOf(kolonne to data)
                    mapOf(
                        "kolonne" to kolonne,
                        "opplysning_id" to it.id,
                        "datatype" to datatype.navn(),
                    ) + verdi
                },
            )
        }

        private fun verdiKolonne(
            datatype: Datatype<*>,
            verdi: Any,
        ) = when (datatype) {
            Boolsk -> Pair("verdi_boolsk", verdi)
            Dato ->
                Pair(
                    "verdi_dato",
                    tilPostgresqlTimestamp(verdi),
                )

            Desimaltall -> Pair("verdi_desimaltall", verdi)
            Heltall -> Pair("verdi_heltall", verdi)
            ULID -> Pair("verdi_string", (verdi as Ulid).verdi)
            Penger -> Pair("verdi_string", (verdi as Beløp).toString())
            InntektDataType ->
                Pair(
                    "verdi_jsonb",
                    (verdi as Inntekt).verdi.let {
                        PGobject().apply {
                            type = "jsonb"
                            value = objectMapper.writeValueAsString(it)
                        }
                    },
                )

            Tekst -> Pair("verdi_string", verdi)
        }

        private fun tilPostgresqlTimestamp(verdi: Any) =
            when (val dato = verdi as LocalDate) {
                LocalDate.MIN ->
                    PGobject().apply {
                        type = "timestamp"
                        value = "-infinity"
                    }

                LocalDate.MAX ->
                    PGobject().apply {
                        type = "timestamp"
                        value = "infinity"
                    }

                else -> dato
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

        val erstattetAv =
            erstattetAv.map { opplysningId ->
                (opplysningMap[opplysningId] ?: this@somOpplysninger.find { it.id == opplysningId }?.toOpplysning()) as Opplysning<T>
            }

        // Create the Opplysning instance
        val opplysning =
            when (status) {
                "Hypotese" ->
                    Hypotese(
                        id,
                        opplysningstype,
                        verdi,
                        gyldighetsperiode,
                        utledetAv,
                        kilde,
                        opprettet,
                    )

                "Faktum" ->
                    Faktum(
                        id,
                        opplysningstype,
                        verdi,
                        gyldighetsperiode,
                        utledetAv,
                        kilde,
                        opprettet,
                    )

                else -> throw IllegalStateException("Ukjent opplysningstype")
            }.also {
                it.erstattesAv(*erstattetAv.toTypedArray())
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
    val opplysingerId: UUID,
    val id: UUID,
    val opplysningstype: Opplysningstype<T>,
    val verdi: T,
    val status: String,
    val gyldighetsperiode: Gyldighetsperiode,
    val utledetAv: UtledningRad? = null,
    val kildeId: UUID? = null,
    val kilde: Kilde? = null,
    val opprettet: LocalDateTime,
    val erstattetAv: List<UUID>,
)
