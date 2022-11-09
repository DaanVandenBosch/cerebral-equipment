package equipment.cerebral.cell.list

import equipment.cerebral.cell.Cell
import equipment.cerebral.cell.ChangeObserver
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
    override val empty: Cell<Boolean> = if (elements.isEmpty()) trueCell() else falseCell()
    override val notEmpty: Cell<Boolean> = if (elements.isNotEmpty()) trueCell() else falseCell()

    override val value: List<E> = elements

    override val changeEvent: ListChangeEvent<E>? get() = null

    override fun addDependent(dependent: Dependent) {
        // We don't remember our dependents because we never need to notify them of changes.
    }

    override fun removeDependent(dependent: Dependent) {
        // Nothing to remove because we don't remember our dependents.
    }

    override fun get(index: Int): E = elements[index]

    override fun observeChange(observer: ChangeObserver<List<E>>): Disposable = nopDisposable()

    override fun observeListChange(observer: ListChangeObserver<E>): Disposable = nopDisposable()

    override fun toString(): String = listCellToString(this)
}
