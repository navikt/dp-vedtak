package no.nav.dagpenger.behandling.mediator.melding

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
import java.util.UUID

interface OpplysningRepository {
    fun lagreOpplysning(opplysning: Opplysning<*>): Int

    fun hentOpplysning(opplysningId: UUID): String
}

class OpplysningRepositoryPostgres : OpplysningRepository {
    override fun hentOpplysning(opplysningId: UUID): String {
        val query =
            queryOf(
                //language=PostgreSQL
                "SELECT * FROM opplysning WHERE id = ?",
                mapOf("id" to opplysningId),
            )
        return "Opplysning"
    }

    override fun lagreOpplysning(opplysning: Opplysning<*>) =
        using(sessionOf(dataSource)) { session ->
            session.transaction { tx ->
                tx.lagreOpplysning(opplysning.id, opplysning.opplysningstype, opplysning.gyldighetsperiode)
                // tx.lagreKilde(opplysning.id, opplysning.kilde)
                tx.lagreVerdi(opplysning.id, opplysning.opplysningstype.datatype, opplysning.verdi)
            }
        }

    private fun TransactionalSession.lagreOpplysning(
        id: UUID,
        opplysningstype: Opplysningstype<*>,
        gyldighetsperiode: Gyldighetsperiode,
    ) = run(
        queryOf(
            //language=PostgreSQL
            """INSERT INTO opplysning (id, opplysningstype, fom, tom) VALUES (:id, :opplysningstype, '-infinity', 'infinity')""",
            mapOf(
                "id" to id,
                "opplysningstype" to opplysningstype.navn,
                // "fom" to gyldighetsperiode.fom.let { if (it == LocalDateTime.MIN) "'-infinity'::timestamptz" else it },
                // "tom" to gyldighetsperiode.tom.let { if (it == LocalDateTime.MAX) "infinity" else it },
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
                INSERT INTO opplysning_verdi (opplysning_id, $kolonne) VALUES (:opplysning_id, :verdi)
                """.trimIndent(),
                mapOf(
                    "opplysning_id" to opplysningId,
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
