package no.nav.dagpenger.behandling.mediator.repository

import kotliquery.Session
import kotliquery.sessionOf
import no.nav.dagpenger.behandling.db.PostgresDataSourceBuilder.dataSource

class PostgresUnitOfWork private constructor(private val session: Session) : UnitOfWork<Session> {
    companion object {
        fun transaction() = PostgresUnitOfWork(sessionOf(dataSource)).apply { begin() }
    }

    private fun begin() = session.connection.begin()

    override fun commit() = session.use { it.connection.commit() }

    override fun rollback() = session.use { it.connection.rollback() }

    override fun <T> inTransaction(block: (Session) -> T) =
        try {
            block(session)
        } catch (e: Exception) {
            rollback()
            throw e
        }
}
