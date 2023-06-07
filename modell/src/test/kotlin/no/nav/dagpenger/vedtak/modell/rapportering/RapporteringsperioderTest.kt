package no.nav.dagpenger.vedtak.modell.rapportering

import io.kotest.matchers.shouldBe
import no.nav.dagpenger.vedtak.hjelpere.februar
import no.nav.dagpenger.vedtak.hjelpere.mai
import no.nav.dagpenger.vedtak.modell.entitet.Timer.Companion.timer
import no.nav.dagpenger.vedtak.modell.hendelser.Rapporteringsdag
import no.nav.dagpenger.vedtak.modell.hendelser.Rapporteringshendelse
import no.nav.dagpenger.vedtak.modell.visitor.PersonVisitor
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.random.Random

internal class RapporteringsperioderTest {
    private lateinit var rapporteringsperioder: Rapporteringsperioder

    private val inspektør get() = RapporteringsdagerVisitor(rapporteringsperioder)

    @BeforeEach
    fun setup() {
        rapporteringsperioder = Rapporteringsperioder().also {
            it.håndter(
                rapporteringshendelse(
                    Rapporteringsdag(1.februar(2023), false, 3),
                    Rapporteringsdag(2.februar(2023), false, 0),
                ),
            )
        }
    }

    @Test
    fun `kan merge to rapporteringsperioder`() {
        rapporteringsperioder.håndter(
            rapporteringshendelse(
                Rapporteringsdag(1.februar(2023), false, 6),
                Rapporteringsdag(2.februar(2023), false, 0),
            ),
        )

        val dager = inspektør.dager
        dager.size shouldBe 2
        dager.first().dato() shouldBe 1.februar(2023)
        dager.first().arbeidstimer() shouldBe 6.timer
        dager.last().dato() shouldBe 2.februar(2023)
        dager.last().arbeidstimer() shouldBe 0.timer
        inspektør.antallRapporteringsperioder shouldBe 1
    }

    @Test
    fun `kan legge til rapportering`() {
        rapporteringsperioder.håndter(
            rapporteringshendelse(
                Rapporteringsdag(3.februar(2023), false, 6),
                Rapporteringsdag(4.februar(2023), false, 0),
            ),
        )
        val dager = inspektør.dager
        dager.size shouldBe 4
        dager.first().dato() shouldBe 1.februar(2023)
        dager.last().dato() shouldBe 4.februar(2023)
        inspektør.antallRapporteringsperioder shouldBe 2
    }

    @Test
    fun `skal bevare historikk`() {
        val mai = (1 until 32).map { dag -> dag.mai(2023) }
        val maiTilRapporteringsperioder = mai.partition { it < 14.mai(2023) }
        val førsteRapporteringsHendelse =
            maiTilRapporteringsperioder.first.map { Rapporteringsdag(it, Random.nextBoolean(), Random.nextInt()) }
        val andreRapporteringsHendelse =
            maiTilRapporteringsperioder.second.map { Rapporteringsdag(it, Random.nextBoolean(), Random.nextInt()) }
        rapporteringsperioder.håndter(
            rapporteringshendelse(*førsteRapporteringsHendelse.toTypedArray()),
        )
        rapporteringsperioder.håndter(
            rapporteringshendelse(*andreRapporteringsHendelse.toTypedArray()),
        )

        val dager = inspektør.dager
        dager.size shouldBe 33
        dager.first().dato() shouldBe 1.februar(2023)
        dager.last().dato() shouldBe 31.mai(2023)
        inspektør.antallRapporteringsperioder shouldBe 3
    }

    private fun rapporteringshendelse(vararg rapporteringsdager: Rapporteringsdag) = Rapporteringshendelse(
        ident = "123",
        rapporteringsId = UUID.randomUUID(),
        rapporteringsdager = rapporteringsdager.toList(),

    )

    private class RapporteringsdagerVisitor(rapporteringsperioder: Rapporteringsperioder) : PersonVisitor {

        val dager = mutableListOf<Dag>()
        var antallRapporteringsperioder = 0

        init {
            rapporteringsperioder.accept(this)
        }

        override fun preVisitRapporteringsperiode(rapporteringsperiode: Rapporteringsperiode) {
            antallRapporteringsperioder++
        }

        override fun visitArbeidsdag(arbeidsdag: Arbeidsdag) {
            dager.add(arbeidsdag)
        }

        override fun visitFraværsdag(fraværsdag: Fraværsdag) {
            dager.add(fraværsdag)
        }

        override fun visitHelgedag(helgedag: Helgedag) {
            dager.add(helgedag)
        }
    }
}
