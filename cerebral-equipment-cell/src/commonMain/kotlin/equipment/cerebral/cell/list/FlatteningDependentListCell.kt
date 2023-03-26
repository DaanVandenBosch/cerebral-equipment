package equipment.cerebral.cell.list

import equipment.cerebral.cell.AbstractFlatteningDependentCell
import equipment.cerebral.cell.Cell
import equipment.cerebral.cell.DependentCell
import equipment.cerebral.cell.MutationManager
import equipment.cerebral.cell.unsafeAssertNotNull

/**
 * Similar to [DependentListCell], except that this cell's computeElements returns a [ListCell].
 */
// IMPROVE: Improve performance when computed list cell changes. At the moment a list change is
// generated that just pretends the whole list has changed.
internal class FlatteningDependentListCell<E>(
    vararg dependencies: Cell<*>,
    computeElements: () -> ListCell<E>,
) :
    AbstractFlatteningDependentCell<List<E>, ListCell<E>>(
        dependencies,
        computeElements
    ),
    ListCell<E> {

    private var _changes: MutableList<ListChange<E>> = mutableListOf()
    override val changes: List<ListChange<E>>
        get() {
            computeValueAndLastChanged()
            return _changes
        }

    // IMPROVE: size should only change when necessary.
    private var _size: Cell<Int>? = null
    override val size: Cell<Int>
        get() {
            if (_size == null) {
                _size = DependentCell(this) { value.size }
            }

            return unsafeAssertNotNull(_size)
        }

    // IMPROVE: isEmpty should only change when necessary.
    private var _isEmpty: Cell<Boolean>? = null
    override val isEmpty: Cell<Boolean>
        get() {
            if (_isEmpty == null) {
                _isEmpty = DependentCell(this) { value.isEmpty() }
            }

            return unsafeAssertNotNull(_isEmpty)
        }

    // IMPROVE: isNotEmpty should only change when necessary.
    private var _isNotEmpty: Cell<Boolean>? = null
    override val isNotEmpty: Cell<Boolean>
        get() {
            if (_isNotEmpty == null) {
                _isNotEmpty = DependentCell(this) { value.isNotEmpty() }
            }

            return unsafeAssertNotNull(_isNotEmpty)
        }

    override fun updateValueAndLastChanged(oldValue: List<E>?, newValue: List<E>) {
        // Make a copy because this value is later used as the "removed" field of a list change.
        val newElements = newValue.toList()
        val oldElements = oldValue ?: emptyList()
        valueInternal = newElements

        val currentMutationId = MutationManager.currentMutationId

        if (lastChangedInternal != currentMutationId) {
            lastChangedInternal = currentMutationId
            _changes.clear()
        }

        _changes.add(
            ListChange(
                index = 0,
                prevSize = oldElements.size,
                removed = oldElements,
                inserted = newValue,
            )
        )
    }

    override fun toString(): String = listCellToString(this)
}
