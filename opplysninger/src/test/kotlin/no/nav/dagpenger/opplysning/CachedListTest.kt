package no.nav.dagpenger.opplysning

import io.kotest.matchers.collections.shouldContainExactly
import org.junit.jupiter.api.Test

class CachedListTest {
    @Test
    fun `cacher lister som er mutable på utsiden`() {
        var basertPå = mutableListOf(1)
        var externalList = mutableListOf(2, 3)
        val cachedList = CachedList { basertPå + externalList.filter { it != 5 } }

        cachedList.shouldContainExactly(1, 2, 3)

        externalList.add(4)
        cachedList.shouldContainExactly(1, 2, 3)

        cachedList.refresh()
        cachedList.shouldContainExactly(1, 2, 3, 4)
    }
}
