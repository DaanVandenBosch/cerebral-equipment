package equipment.cerebral.cell.list

import equipment.cerebral.cell.Dependency
import equipment.cerebral.cell.LeafDependent
import equipment.cerebral.cell.MutationManager
import equipment.cerebral.cell.disposable.TrackedDisposable

/**
 * Calls [callback] when [dependency] changes.
 */
internal class CallbackListChangeObserver<E>(
    private val dependency: ListCell<E>,
    private val callback: (List<ListChange<E>>) -> Unit,
) : TrackedDisposable(), LeafDependent {

    init {
        dependency.addDependent(this)
    }

    override fun dispose() {
        dependency.removeDependent(this)
        super.dispose()
    }

    override fun dependencyInvalidated(dependency: Dependency<*>) {
        MutationManager.invalidated(this)
    }

    override fun dependenciesChanged() {
        if (dependency.lastChanged == MutationManager.currentMutationId) {
            callback(dependency.changes)
        }
    }
}
