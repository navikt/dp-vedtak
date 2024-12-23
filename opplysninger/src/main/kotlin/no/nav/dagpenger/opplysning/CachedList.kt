package no.nav.dagpenger.opplysning

class CachedList<T>(
    val compute: () -> List<T>,
) : List<T> {
    private var cachedValue: List<T>? = null
    private val value: List<T>
        get() = cachedValue ?: refresh()

    fun refresh(): List<T> {
        cachedValue = compute()
        return cachedValue!!
    }

    override val size get() = value.size

    override fun contains(element: T) = value.contains(element)

    override fun containsAll(elements: Collection<T>) = value.containsAll(elements)

    override fun get(index: Int) = value[index]

    override fun indexOf(element: T) = value.indexOf(element)

    override fun isEmpty() = value.isEmpty()

    override fun iterator() = value.iterator()

    override fun lastIndexOf(element: T) = value.lastIndexOf(element)

    override fun listIterator() = value.listIterator()

    override fun listIterator(index: Int) = value.listIterator(index)

    override fun subList(
        fromIndex: Int,
        toIndex: Int,
    ) = value.subList(fromIndex, toIndex)
}
