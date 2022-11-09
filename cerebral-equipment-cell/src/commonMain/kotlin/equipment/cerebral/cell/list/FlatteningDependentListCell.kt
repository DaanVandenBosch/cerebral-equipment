package equipment.cerebral.cell.list

import equipment.cerebral.cell.AbstractFlatteningDependentCell
import equipment.cerebral.cell.CallbackChangeObserver
import equipment.cerebral.cell.Cell
import equipment.cerebral.cell.ChangeObserver
import equipment.cerebral.cell.DependentCell
import equipment.cerebral.cell.MutationManager
import equipment.cerebral.cell.disposable.Disposable
import equipment.cerebral.cell.unsafeAssertNotNull
import equipment.cerebral.cell.unsafeCast

/**
 * Similar to [DependentListCell], except that this cell's computeElements returns a [ListCell].
 */
// IMPROVE: Improve performance when transitive cell changes. At the moment a change event is
// generated that just pretends the whole list has changed.
internal class FlatteningDependentListCell<E>(
    vararg dependencies: Cell<*>,
    computeElements: () -> ListCell<E>,
) :
    AbstractFlatteningDependentCell<List<E>, ListCell<E>, ListChangeEvent<E>>(
        dependencies,
        computeElements
    ),
    ListCell<E> {

    /** Mutation ID during which the current list of changes was created. */
    private var changesMutationId: Long = -1

    private var _size: Cell<Int>? = null
    override val size: Cell<Int>
        get() {
            if (_size == null) {
                _size = DependentCell(this) { value.size }
            }

            return unsafeAssertNotNull(_size)
        }

    private var _empty: Cell<Boolean>? = null
    override val empty: Cell<Boolean>
        get() {
            if (_empty == null) {
                _empty = DependentCell(this) { value.isEmpty() }
            }

            return unsafeAssertNotNull(_empty)
        }

    private var _notEmpty: Cell<Boolean>? = null
    override val notEmpty: Cell<Boolean>
        get() {
            if (_notEmpty == null) {
                _notEmpty = DependentCell(this) { value.isNotEmpty() }
            }

            return unsafeAssertNotNull(_notEmpty)
        }

    override fun observeChange(observer: ChangeObserver<List<E>>): Disposable =
        observeListChange(observer)

    override fun observeListChange(observer: ListChangeObserver<E>): Disposable =
        CallbackChangeObserver(this, observer)

    override fun toString(): String = listCellToString(this)

    override fun updateValueAndEvent(oldValue: List<E>?, newValue: List<E>) {
        // Make a copy because this value is later used as the "removed" field of a list change.
        val newElements = newValue.toList()
        val oldElements = oldValue ?: emptyList()
        valueInternal = newElements

        val event = changeEventInternal

        val changes =
            if (event == null || changesMutationId != MutationManager.currentMutationId) {
                changesMutationId = MutationManager.currentMutationId
                mutableListOf()
            } else {
                // Reuse the same list of changes during a mutation.
                // This cast is safe because we know we always instantiate our change event with
                // a mutable list.
                unsafeCast<MutableList<ListChange<E>>>(event.changes)
            }

        changes.add(
            ListChange(
                index = 0,
                prevSize = oldElements.size,
                removed = oldElements,
                inserted = newValue,
            )
        )

        changeEventInternal = ListChangeEvent(newValue, changes)
    }
}
