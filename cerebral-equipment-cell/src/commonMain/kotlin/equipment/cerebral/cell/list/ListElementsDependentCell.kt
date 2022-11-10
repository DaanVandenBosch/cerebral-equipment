package equipment.cerebral.cell.list

import equipment.cerebral.cell.AbstractCell
import equipment.cerebral.cell.Cell
import equipment.cerebral.cell.Dependency
import equipment.cerebral.cell.Dependent
import equipment.cerebral.cell.MutationManager
import equipment.cerebral.cell.splice

/**
 * Depends on a [ListCell] and zero or more cells per element in the list.
 */
internal class ListElementsDependentCell<E>(
    private val list: ListCell<E>,
    private val extractCells: (element: E) -> Array<out Cell<*>>,
) : AbstractCell<List<E>>(), Dependent {
    /** An array of dependencies per [list] element, extracted by [extractCells]. */
    private val elementDependencies = mutableListOf<Array<out Dependency<*>>>()

    private var valid = false
    private var listInvalidated = false

    override val value: List<E>
        get() {
            updateElementDependenciesAndChanges()
            return list.value
        }

    private var _lastChanged: Long = -1
    override val lastChanged: Long
        get() {
            updateElementDependenciesAndChanges()
            return _lastChanged
        }

    private fun updateElementDependenciesAndChanges() {
        if (!valid) {
            if (listInvalidated && list.lastChanged == MutationManager.currentMutationId) {
                // At this point we can remove this dependent from the removed elements'
                // dependencies and add it to the newly inserted elements' dependencies.
                for (change in list.changes) {
                    for (i in change.index until (change.index + change.removed.size)) {
                        for (elementDependency in elementDependencies[i]) {
                            elementDependency.removeDependent(this)
                        }
                    }

                    val inserted = change.inserted.map(extractCells)

                    elementDependencies.splice(
                        startIndex = change.index,
                        amount = change.removed.size,
                        elements = inserted,
                    )

                    for (elementDependencies in inserted) {
                        for (elementDependency in elementDependencies) {
                            elementDependency.addDependent(this)
                        }
                    }
                }
            }

            _lastChanged = MutationManager.currentMutationId

            // Reset for the next change wave.
            listInvalidated = false

            // We stay invalid if we have no dependents to ensure our changes are always recomputed.
            valid = dependents.isNotEmpty()
        }
    }

    override fun addDependent(dependent: Dependent) {
        if (dependents.isEmpty()) {
            // Once we have our first dependent, we start depending on our own dependencies.
            list.addDependent(this)

            for (element in list.value) {
                val dependencies = extractCells(element)

                for (dependency in dependencies) {
                    dependency.addDependent(this)
                }

                elementDependencies.add(dependencies)
            }
        }

        super.addDependent(dependent)
    }

    override fun removeDependent(dependent: Dependent) {
        super.removeDependent(dependent)

        if (dependents.isEmpty()) {
            valid = false
            listInvalidated = false

            // At this point we have no more dependents, so we can stop depending on our own
            // dependencies.
            for (dependencies in elementDependencies) {
                for (dependency in dependencies) {
                    dependency.removeDependent(this)
                }
            }

            elementDependencies.clear()
            list.removeDependent(this)
        }
    }

    override fun dependencyInvalidated(dependency: Dependency<*>) {
        valid = false

        if (dependency === list) {
            listInvalidated = true
        }

        emitDependencyInvalidated()
    }
}
