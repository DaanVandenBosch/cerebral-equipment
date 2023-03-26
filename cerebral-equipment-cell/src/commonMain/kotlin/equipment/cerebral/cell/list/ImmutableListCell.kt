package equipment.cerebral.cell.list

import equipment.cerebral.cell.Cell
import equipment.cerebral.cell.Dependency
import equipment.cerebral.cell.Dependent
import equipment.cerebral.cell.cell
import equipment.cerebral.cell.disposable.Disposable
import equipment.cerebral.cell.disposable.nopDisposable
import equipment.cerebral.cell.falseCell
import equipment.cerebral.cell.trueCell

internal class ImmutableListCell<E>(
    private val elements: List<E>,
) : Dependency<List<E>>, ListCell<E> {

    override val size: Cell<Int> = cell(elements.size)
    override val isEmpty: Cell<Boolean> = if (elements.isEmpty()) trueCell() else falseCell()
    override val isNotEmpty: Cell<Boolean> = if (elements.isNotEmpty()) trueCell() else falseCell()

    override val value: List<E> = elements

    override val lastChanged: Long get() = -1

    override val changes: List<ListChange<E>> get() = emptyList()

    override fun addDependent(dependent: Dependent) {
        // We don't remember our dependents because we never need to notify them of changes.
    }

    override fun removeDependent(dependent: Dependent) {
        // Nothing to remove because we don't remember our dependents.
    }

    override fun get(index: Int): E = elements[index]

    override fun observeChange(observer: () -> Unit): Disposable = nopDisposable()

    override fun toString(): String = listCellToString(this)
}
