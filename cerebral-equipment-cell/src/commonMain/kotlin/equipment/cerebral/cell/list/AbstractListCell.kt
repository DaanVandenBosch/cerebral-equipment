package equipment.cerebral.cell.list

import equipment.cerebral.cell.AbstractCell
import equipment.cerebral.cell.Cell
import equipment.cerebral.cell.DependentCell
import equipment.cerebral.cell.unsafeAssertNotNull

internal abstract class AbstractListCell<E> : AbstractCell<List<E>>(), ListCell<E> {

    private var _size: Cell<Int>? = null
    final override val size: Cell<Int>
        get() {
            if (_size == null) {
                _size = DependentCell(this) { value.size }
            }

            return unsafeAssertNotNull(_size)
        }

    private var _empty: Cell<Boolean>? = null
    final override val empty: Cell<Boolean>
        get() {
            if (_empty == null) {
                _empty = DependentCell(this) { value.isEmpty() }
            }

            return unsafeAssertNotNull(_empty)
        }

    private var _notEmpty: Cell<Boolean>? = null
    final override val notEmpty: Cell<Boolean>
        get() {
            if (_notEmpty == null) {
                _notEmpty = DependentCell(this) { value.isNotEmpty() }
            }

            return unsafeAssertNotNull(_notEmpty)
        }

    override fun toString(): String = listCellToString(this)
}
