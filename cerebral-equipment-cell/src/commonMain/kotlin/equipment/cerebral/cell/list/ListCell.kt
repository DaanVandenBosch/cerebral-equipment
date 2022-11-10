package equipment.cerebral.cell.list

import equipment.cerebral.cell.Cell
import equipment.cerebral.cell.disposable.Disposable

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

    /**
     * List variant of [Cell.observeChange].
     */
    fun observeListChange(observer: (List<ListChange<E>>) -> Unit): Disposable

    operator fun contains(element: @UnsafeVariance E): Boolean = element in value
}
