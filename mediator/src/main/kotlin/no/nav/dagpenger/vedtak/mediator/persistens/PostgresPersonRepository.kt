package no.nav.dagpenger.vedtak.mediator.persistens

import kotliquery.Query
import kotliquery.TransactionalSession
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.dagpenger.vedtak.modell.Person
import no.nav.dagpenger.vedtak.modell.PersonIdentifikator
import no.nav.dagpenger.vedtak.modell.PersonIdentifikator.Companion.tilPersonIdentfikator
import no.nav.dagpenger.vedtak.modell.visitor.PersonVisitor
import javax.sql.DataSource

class PostgresPersonRepository(private val dataSource: DataSource) : PersonRepository {
    override fun hent(ident: PersonIdentifikator): Person? {
        return using(sessionOf(dataSource)) { session ->
            session.run(
                queryOf(
                    //language=PostgreSQL
                    statement = """
                     SELECT * FROM person WHERE ident = :ident
                    """.trimIndent(),
                    paramMap = mapOf("ident" to ident.identifikator()),
                ).map { row ->
                    Person(
                        row.string("ident").tilPersonIdentfikator(),
                    )
                }.asSingle,
            )
        }
    }

    override fun lagre(person: Person) {
        val lagrePersonVisitor = LagrePersonVisitor(person)

        using(sessionOf(dataSource)) { session ->
            session.transaction { transactionalSession: TransactionalSession ->
                lagrePersonVisitor.queries.forEach {
                    transactionalSession.run(it.asUpdate)
                }
            }
        }
    }
}

class LagrePersonVisitor(person: Person) : PersonVisitor {

    // private val personIdentifikator =
    val queries = mutableListOf<Query>()

    init {
        person.accept(this)
    }

    override fun visitPerson(personIdentifikator: PersonIdentifikator) {
        queries.add(
            queryOf(
                //language=PostgreSQL
                statement = """INSERT INTO person (ident) VALUES (:ident) ON CONFLICT DO NOTHING""",
                paramMap = mapOf("ident" to personIdentifikator.identifikator()),
            ),
        )
    }
}
