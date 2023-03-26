package equipment.cerebral.cell.list

import equipment.cerebral.cell.AbstractCell
import equipment.cerebral.cell.Cell
import equipment.cerebral.cell.DependentCell
import equipment.cerebral.cell.unsafeAssertNotNull

internal abstract class AbstractListCell<E> : AbstractCell<List<E>>(), ListCell<E> {

    // IMPROVE: size should only change when necessary.
    private var _size: Cell<Int>? = null
    final override val size: Cell<Int>
        get() {
            if (_size == null) {
                _size = DependentCell(this) { value.size }
            }

            return unsafeAssertNotNull(_size)
        }

    // IMPROVE: isEmpty should only change when necessary.
    private var _isEmpty: Cell<Boolean>? = null
    final override val isEmpty: Cell<Boolean>
        get() {
            if (_isEmpty == null) {
                _isEmpty = DependentCell(this) { value.isEmpty() }
            }

            return unsafeAssertNotNull(_isEmpty)
        }

    // IMPROVE: isNotEmpty should only change when necessary.
    private var _isNotEmpty: Cell<Boolean>? = null
    final override val isNotEmpty: Cell<Boolean>
        get() {
            if (_isNotEmpty == null) {
                _isNotEmpty = DependentCell(this) { value.isNotEmpty() }
            }

            return unsafeAssertNotNull(_isNotEmpty)
        }

    override fun toString(): String = listCellToString(this)
}
