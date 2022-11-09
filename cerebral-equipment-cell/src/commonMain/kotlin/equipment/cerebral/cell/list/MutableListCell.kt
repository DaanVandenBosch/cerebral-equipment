package equipment.cerebral.cell.list

import equipment.cerebral.cell.MutableCell

interface MutableListCell<E> : ListCell<E>, MutableCell<List<E>> {
    operator fun set(index: Int, element: E): E

    fun add(element: E)

    fun add(index: Int, element: E)

    fun remove(element: E): Boolean

    fun removeAt(index: Int): E

    fun replaceAll(elements: Iterable<E>)

    fun replaceAll(elements: Sequence<E>)

    fun splice(fromIndex: Int, removeCount: Int, newElement: E)

    fun clear()

    fun sortWith(comparator: Comparator<E>)
}
