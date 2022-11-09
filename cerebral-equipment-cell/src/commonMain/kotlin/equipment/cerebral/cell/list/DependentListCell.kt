package equipment.cerebral.cell.list

import equipment.cerebral.cell.Cell
import equipment.cerebral.cell.Dependency
import equipment.cerebral.cell.Dependent
import equipment.cerebral.cell.MutationManager
import equipment.cerebral.cell.unsafeCast

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
            computeValueAndEvent()
            return _value
        }

    private var _changeEvent: ListChangeEvent<E>? = null
    override val changeEvent: ListChangeEvent<E>?
        get() {
            computeValueAndEvent()
            return _changeEvent
        }

    /** Mutation ID during which the current list of changes was created. */
    private var changesMutationId: Long = -1

    private fun computeValueAndEvent() {
        if (!valid) {
            val oldElements = _value
            val newElements = computeElements()
            _value = newElements

            val event = _changeEvent

            val changes =
                if (event == null || changesMutationId != MutationManager.currentMutationId) {
                    changesMutationId = MutationManager.currentMutationId
                    mutableListOf()
                } else {
                    // Reuse the same list of changes during a mutation.
                    // This cast is safe because we know we always instantiate our change event with
                    // a mutable list.
                    unsafeCast<MutableList<ListChange<E>>>(event.changes)
                }

            changes.add(
                ListChange(
                    index = 0,
                    prevSize = oldElements.size,
                    removed = oldElements,
                    inserted = newElements,
                )
            )
            _changeEvent = ListChangeEvent(newElements, changes)
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
