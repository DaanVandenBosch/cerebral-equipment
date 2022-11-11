package equipment.cerebral.cell

import equipment.cerebral.cell.disposable.Disposable

abstract class AbstractCell<T> : AbstractDependency<T>(), Cell<T> {
    override fun observeChange(observer: () -> Unit): Disposable =
        SingleCellObserver(this, observer)

    override fun toString(): String = cellToString(this)
}
