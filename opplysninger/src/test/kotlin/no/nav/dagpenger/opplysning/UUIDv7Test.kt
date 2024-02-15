package no.nav.dagpenger.opplysning

import com.fasterxml.uuid.UUIDType
import com.fasterxml.uuid.impl.UUIDUtil
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.equals.shouldNotBeEqual
import org.junit.jupiter.api.Test
import java.util.UUID

class UUIDv7Test {
    @Test
    fun nyId() {
        val uuidV7 = UUIDv7.ny()
        shouldNotThrow<IllegalArgumentException> { UUID.fromString(uuidV7.toString()) }
        uuidV7 shouldNotBeEqual UUIDv7.ny()
        UUIDUtil.typeOf(uuidV7) shouldBeEqual UUIDType.TIME_BASED_EPOCH
    }
}
