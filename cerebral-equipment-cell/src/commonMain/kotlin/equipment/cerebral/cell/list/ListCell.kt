package equipment.cerebral.cell.list

import equipment.cerebral.cell.Cell
import equipment.cerebral.cell.disposable.Disposable

interface ListCell<out E> : Cell<List<E>> {
    override val value: List<E>

    override val changeEvent: ListChangeEvent<E>?

    val size: Cell<Int>

    val empty: Cell<Boolean>

    val notEmpty: Cell<Boolean>

    operator fun get(index: Int): E = value[index]

    /**
     * List variant of [Cell.observeChange].
     */
    // Exists solely because function parameters are invariant.
    fun observeListChange(observer: ListChangeObserver<E>): Disposable

    operator fun contains(element: @UnsafeVariance E): Boolean = element in value
}
