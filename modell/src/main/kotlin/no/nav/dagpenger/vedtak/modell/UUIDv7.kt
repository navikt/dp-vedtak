package no.nav.dagpenger.vedtak.modell

import com.fasterxml.uuid.Generators
import java.util.UUID

object UUIDv7 {
    private val idGenerator = Generators.timeBasedEpochGenerator()

    fun ny(): UUID = idGenerator.generate()
}
