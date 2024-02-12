package no.nav.dagpenger.vedtak.modell

import io.kotest.assertions.throwables.shouldNotThrow
import org.junit.jupiter.api.Test
import java.util.UUID

class UUIDv7Test {
    @Test
    fun nyId() {
        val uuidV7 = UUIDv7.ny()
        shouldNotThrow<IllegalArgumentException> { UUID.fromString(uuidV7.toString()) }
    }
}
