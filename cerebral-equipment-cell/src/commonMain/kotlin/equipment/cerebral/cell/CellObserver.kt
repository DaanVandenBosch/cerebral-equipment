package equipment.cerebral.cell

import equipment.cerebral.cell.disposable.TrackedDisposable

/**
 * Calls [callback] when one or more cells in [dependencies] change.
 */
internal class CellObserver(
    private vararg val dependencies: Cell<*>,
    private val callback: () -> Unit,
) : TrackedDisposable(), LeafDependent {

    init {
        for (dependency in dependencies) {
            dependency.addDependent(this)
        }
    }

    override fun dispose() {
        for (dependency in dependencies) {
            dependency.removeDependent(this)
        }

        super.dispose()
    }

    override fun dependencyInvalidated(dependency: Dependency<*>) {
        MutationManager.invalidated(this)
    }

    override fun dependenciesChanged() {
        var changed = false

        // We loop through all dependencies instead of breaking early to ensure they're valid again.
        for (dependency in dependencies) {
            if (dependency.lastChanged == MutationManager.currentMutationId) {
                changed = true
            }
        }

        if (changed) {
            callback()
        }
    }
}
