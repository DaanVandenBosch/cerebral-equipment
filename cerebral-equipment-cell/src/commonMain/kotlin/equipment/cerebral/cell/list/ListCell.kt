package equipment.cerebral.cell.list

import equipment.cerebral.cell.Cell

interface ListCell<out E> : Cell<List<E>> {
    override val value: List<E>

    /**
     * This property is not meant to be accessed from typical application code. The current list of
     * changes for this dependency. Only valid during a mutation.
     */
    val changes: List<ListChange<E>>

    val size: Cell<Int>

    val empty: Cell<Boolean>

    val notEmpty: Cell<Boolean>

    operator fun get(index: Int): E = value[index]

    operator fun contains(element: @UnsafeVariance E): Boolean = element in value
}
