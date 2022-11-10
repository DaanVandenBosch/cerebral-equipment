package equipment.cerebral.cell

import equipment.cerebral.cell.disposable.TrackedDisposable

/**
 * Calls [callback] when [dependency] changes.
 */
internal class CallbackChangeObserver<T>(
    private val dependency: Cell<T>,
    private val callback: (T) -> Unit,
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
            callback(dependency.value)
        }
    }
}
