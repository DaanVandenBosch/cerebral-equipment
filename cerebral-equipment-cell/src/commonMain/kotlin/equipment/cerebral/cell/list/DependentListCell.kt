package equipment.cerebral.cell.list

import equipment.cerebral.cell.Cell
import equipment.cerebral.cell.Dependency
import equipment.cerebral.cell.Dependent
import equipment.cerebral.cell.MutationManager

/**
 * ListCell of which the value depends on 0 or more other cells.
 */
internal class DependentListCell<E>(
    private vararg val dependencies: Cell<*>,
    private val computeElements: () -> List<E>,
) : AbstractListCell<E>(), Dependent {

    private var valid = false

    private var _value: List<E> = emptyList()
    override val value: List<E>
        get() {
            computeValueAndChanges()
            return _value
        }

    private var _lastChanged: Long = -1
    override val lastChanged: Long
        get() {
            computeValueAndChanges()
            return _lastChanged
        }

    private val _changes: MutableList<ListChange<E>> = mutableListOf()
    override val changes: List<ListChange<E>>
        get() {
            computeValueAndChanges()
            return _changes
        }

    private fun computeValueAndChanges() {
        if (!valid) {
            val oldElements = _value
            val newElements = computeElements()
            _value = newElements

            val currentMutationId = MutationManager.currentMutationId

            // Only clear changes once during each mutation.
            if (_lastChanged != currentMutationId) {
                _lastChanged = currentMutationId
                _changes.clear()
            }

            _changes.add(
                ListChange(
                    index = 0,
                    prevSize = oldElements.size,
                    removed = oldElements,
                    inserted = newElements,
                )
            )
            valid = dependents.isNotEmpty()
        }
    }

    override fun addDependent(dependent: Dependent) {
        if (dependents.isEmpty()) {
            for (dependency in dependencies) {
                dependency.addDependent(this)
            }
        }

        super.addDependent(dependent)
    }

    override fun removeDependent(dependent: Dependent) {
        super.removeDependent(dependent)

        if (dependents.isEmpty()) {
            valid = false

            for (dependency in dependencies) {
                dependency.removeDependent(this)
            }
        }
    }

    override fun dependencyInvalidated(dependency: Dependency<*>) {
        valid = false
        emitDependencyInvalidated()
    }
}
