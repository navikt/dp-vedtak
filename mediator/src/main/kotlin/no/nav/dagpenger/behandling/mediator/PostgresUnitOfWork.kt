package no.nav.dagpenger.behandling.mediator

import kotliquery.TransactionalSession
import kotliquery.sessionOf
import no.nav.dagpenger.behandling.db.PostgresDataSourceBuilder

class PostgresUnitOfWork(private val session: TransactionalSession) : UnitOfWork<TransactionalSession> {
    companion object {
        fun start() = PostgresUnitOfWork(sessionOf(PostgresDataSourceBuilder.dataSource).transaction { it })
    }

    override fun <T> inTransaction(block: (TransactionalSession) -> T) =
        // session.transaction { tx ->
        try {
            block(session)
        } catch (e: Exception) {
            println(e)
            throw e
        }
    // }
}
