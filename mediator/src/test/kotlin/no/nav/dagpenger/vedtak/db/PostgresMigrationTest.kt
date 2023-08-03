package no.nav.dagpenger.vedtak.db

import no.nav.dagpenger.vedtak.db.Postgres.withCleanDb
import no.nav.dagpenger.vedtak.db.PostgresDataSourceBuilder.runMigration
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class PostgresMigrationTest {
    @Test
    fun `Migration scripts are applied successfully`() {
        withCleanDb {
            val migrations = runMigration()
            Assertions.assertEquals(2, migrations)
        }
    }
}
