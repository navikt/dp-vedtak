package no.nav.dagpenger.vedtak.iverksetting.mediator.persistens

import io.kotest.matchers.nulls.shouldNotBeNull
import no.nav.dagpenger.vedtak.assertDeepEquals
import no.nav.dagpenger.vedtak.db.Postgres.withMigratedDb
import no.nav.dagpenger.vedtak.db.PostgresDataSourceBuilder
import no.nav.dagpenger.vedtak.iverksetting.Iverksetting
import no.nav.dagpenger.vedtak.iverksetting.IverksettingsVedtak
import no.nav.dagpenger.vedtak.iverksetting.hendelser.VedtakFattetHendelse
import no.nav.dagpenger.vedtak.mediator.persistens.PostgresPersonRepository
import no.nav.dagpenger.vedtak.modell.Dagpengerettighet
import no.nav.dagpenger.vedtak.modell.Person
import no.nav.dagpenger.vedtak.modell.PersonIdentifikator.Companion.tilPersonIdentfikator
import no.nav.dagpenger.vedtak.modell.PersonObserver
import no.nav.dagpenger.vedtak.modell.entitet.Beløp.Companion.beløp
import no.nav.dagpenger.vedtak.modell.entitet.Dagpengeperiode
import no.nav.dagpenger.vedtak.modell.entitet.Timer.Companion.timer
import no.nav.dagpenger.vedtak.modell.hendelser.DagpengerInnvilgetHendelse
import no.nav.dagpenger.vedtak.modell.vedtak.VedtakObserver
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID

class PostgresIverksettingRepositoryTest {

    private val testIdent = "12345678911".tilPersonIdentfikator()
    private val observatør = TestObservatør()

    @Test
    fun `Lagre og hente iverksetting`() {
        val person = Person(ident = testIdent).also { it.addObserver(observatør) }
        val idag = LocalDate.now()
        person.håndter(
            DagpengerInnvilgetHendelse(
                ident = testIdent.identifikator(),
                behandlingId = UUID.randomUUID(),
                vedtakstidspunkt = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS),
                virkningsdato = idag,
                dagpengerettighet = Dagpengerettighet.Ordinær,
                dagsats = 800.beløp,
                stønadsdager = Dagpengeperiode(52).tilStønadsdager(),
                vanligArbeidstidPerDag = 8.timer,
                egenandel = 800.beløp * 3,
            ),
        )
        withMigratedDb {
            val postgresPersonRepository = PostgresPersonRepository(dataSource = PostgresDataSourceBuilder.dataSource)
            postgresPersonRepository.lagre(person)
            val postgresIverksettingRepository = PostgresIverksettingRepository(PostgresDataSourceBuilder.dataSource)
            val vedtakId = observatør.vedtak.first().vedtakId
            val iverksetting = Iverksetting(vedtakId = vedtakId, ident = testIdent.identifikator())

            iverksetting.håndter(
                VedtakFattetHendelse(
                    testIdent.identifikator(),
                    IverksettingsVedtak(
                        vedtakId = vedtakId,
                        behandlingId = UUID.randomUUID(),
                        utbetalingsdager = emptyList(),
                        utfall = IverksettingsVedtak.Utfall.Innvilget,
                        vedtakstidspunkt = LocalDateTime.now(),
                        virkningsdato = LocalDate.now(),
                    ),
                ),
            )

            postgresIverksettingRepository.lagre(iverksetting)

            val rehydrertIverksetting = postgresIverksettingRepository.hent(vedtakId).shouldNotBeNull()
            assertDeepEquals(
                iverksetting,
                rehydrertIverksetting,
            )

            postgresIverksettingRepository.lagre(rehydrertIverksetting)
        }
    }

    private class TestObservatør : PersonObserver {

        val vedtak = mutableListOf<VedtakObserver.VedtakFattet>()
        override fun vedtakFattet(ident: String, vedtakFattet: VedtakObserver.VedtakFattet) {
            vedtak.add(vedtakFattet)
        }
    }
}
