package equipment.cerebral.cell

import equipment.cerebral.cell.disposable.Disposable
import equipment.cerebral.cell.disposable.nopDisposable

internal class ImmutableCell<T>(override val value: T) : Dependency<T>, Cell<T> {
    override val lastChanged: Long get() = -1

    override fun addDependent(dependent: Dependent) {
        // We don't remember our dependents because we never need to notify them of changes.
    }

    override fun removeDependent(dependent: Dependent) {
        // Nothing to remove because we don't remember our dependents.
    }

    override fun observeChange(observer: () -> Unit): Disposable = nopDisposable()

    override fun toString(): String = cellToString(this)
}
