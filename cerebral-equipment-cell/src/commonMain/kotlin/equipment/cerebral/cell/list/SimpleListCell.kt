package equipment.cerebral.cell.list

import equipment.cerebral.cell.MutationManager

/**
 * @param elements The backing list for this [ListCell].
 */
internal class SimpleListCell<E>(
    private var elements: MutableList<E>,
) : AbstractListCell<E>(), MutableListCell<E> {

    override var value: List<E>
        get() = elements
        set(value) {
            replaceAll(value)
        }

    override var lastChanged: Long = -1
        private set

    private var _changes: MutableList<ListChange<E>> = mutableListOf()
    override val changes: List<ListChange<E>>
        get() = _changes

    override operator fun get(index: Int): E =
        elements[index]

    override operator fun set(index: Int, element: E): E {
        checkIndex(index, elements.lastIndex)

        applyChange {
            val removed = elements.set(index, element)

            finalizeChange(
                index,
                prevSize = elements.size,
                removed = listOf(removed),
                inserted = listOf(element),
            )

            return removed
        }
    }

    override fun add(element: E) {
        applyChange {
            val index = elements.size
            elements.add(element)

            finalizeChange(
                index,
                prevSize = index,
                removed = emptyList(),
                inserted = listOf(element),
            )
        }
    }

    override fun add(index: Int, element: E) {
        val prevSize = elements.size
        checkIndex(index, prevSize)

        applyChange {
            elements.add(index, element)

            finalizeChange(index, prevSize, removed = emptyList(), inserted = listOf(element))
        }
    }

    override fun remove(element: E): Boolean {
        val index = elements.indexOf(element)

        return if (index != -1) {
            removeAt(index)
            true
        } else {
            false
        }
    }

    override fun removeAt(index: Int): E {
        checkIndex(index, elements.lastIndex)

        applyChange {
            val prevSize = elements.size
            val removed = elements.removeAt(index)

            finalizeChange(index, prevSize, removed = listOf(removed), inserted = emptyList())
            return removed
        }
    }

    override fun replaceAll(elements: Iterable<E>) {
        applyChange {
            val removed = this.elements

            this.elements = elements.toMutableList()

            finalizeChange(index = 0, prevSize = removed.size, removed, inserted = this.elements)
        }
    }

    override fun replaceAll(elements: Sequence<E>) {
        applyChange {
            val removed = this.elements

            this.elements = elements.toMutableList()

            finalizeChange(index = 0, prevSize = removed.size, removed, inserted = this.elements)
        }
    }

    override fun splice(fromIndex: Int, removeCount: Int, newElement: E) {
        val prevSize = elements.size
        val removed = ArrayList<E>(removeCount)

        // Do this loop outside applyChange because it will throw when any index is out of bounds.
        for (i in fromIndex until (fromIndex + removeCount)) {
            removed.add(elements[i])
        }

        applyChange {
            repeat(removeCount) { elements.removeAt(fromIndex) }
            elements.add(fromIndex, newElement)

            finalizeChange(fromIndex, prevSize, removed, inserted = listOf(newElement))
        }
    }

    override fun clear() {
        if (elements.isEmpty()) {
            return
        }

        applyChange {
            val removed = elements

            elements = mutableListOf()

            finalizeChange(index = 0, prevSize = removed.size, removed, inserted = emptyList())
        }
    }

    override fun sortWith(comparator: Comparator<E>) {
        applyChange {
            val removed = elements.toList()

            elements.sortWith(comparator)

            finalizeChange(
                index = 0,
                prevSize = removed.size,
                removed,
                inserted = elements,
            )
        }
    }

    private fun checkIndex(index: Int, maxIndex: Int) {
        if (index !in 0..maxIndex) {
            throw IndexOutOfBoundsException(
                "Index $index out of bounds for length ${elements.size}",
            )
        }
    }

    private fun finalizeChange(
        index: Int,
        prevSize: Int,
        removed: List<E>,
        inserted: List<E>,
    ) {
        val currentMutationId = MutationManager.currentMutationId

        // Only clear changes once during each mutation.
        if (lastChanged != currentMutationId) {
            lastChanged = currentMutationId
            _changes.clear()
        }

        _changes.add(ListChange(index, prevSize, removed, inserted))
    }
}
