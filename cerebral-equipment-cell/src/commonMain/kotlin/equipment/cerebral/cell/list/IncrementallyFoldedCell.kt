package equipment.cerebral.cell.list

import equipment.cerebral.cell.AbstractCell
import equipment.cerebral.cell.Dependency
import equipment.cerebral.cell.Dependent
import equipment.cerebral.cell.MutationManager
import equipment.cerebral.cell.unsafeCast

/**
 * Cell that depends on a list cell and possibly one or more other cells and can update its value
 * incrementally.
 */
class IncrementallyFoldedCell<out E, T>(
    private val list: ListCell<E>,
    private val otherDependencies: Array<Dependency<*>>,
    /** Called when entire value should be recomputed. */
    private val computeValue: (List<E>) -> T,
    /** Called when value should be updated incrementally. */
    private val updateValue: (oldValue: T, ListChange<E>) -> T,
) : AbstractCell<T>(), Dependent {

    private var valid = false
    private var otherDependencyInvalidated = false
    private var listChangeIndex = 0

    private var _value: T? = null
    override val value: T
        get() {
            computeValueAndLastChanged()
            // We cast instead of asserting _value is non-null because T might actually be a
            // nullable type.
            return unsafeCast(_value)
        }

    private var _lastChanged: Long = -1
    override val lastChanged: Long
        get() {
            computeValueAndLastChanged()
            return _lastChanged
        }

    private fun computeValueAndLastChanged() {
        if (!valid) {
            val currentMutationId = MutationManager.currentMutationId
            val hasDependents = dependents.isNotEmpty()

            if (!hasDependents || otherDependencyInvalidated) {
                _value = computeValue(list.value)
            } else {
                // We cast instead of asserting _value is non-null because T might actually be a
                // nullable type.
                var newValue: T = unsafeCast(_value)
                val listChanges = list.changes

                if (_lastChanged != currentMutationId) {
                    listChangeIndex = 0
                }

                for (change in listChanges.listIterator(listChangeIndex)) {
                    newValue = updateValue(newValue, change)
                }

                listChangeIndex = listChanges.size
                _value = newValue
            }

            otherDependencyInvalidated = false
            // We stay invalid if we have no dependents to ensure our value is always recomputed.
            valid = hasDependents
            _lastChanged = currentMutationId
        }
    }

    override fun addDependent(dependent: Dependent) {
        super.addDependent(dependent)

        if (dependents.size == 1) {
            list.addDependent(this)

            for (otherDependency in otherDependencies) {
                otherDependency.addDependent(this)
            }

            _value = computeValue(list.value)
            valid = true
        }
    }

    override fun removeDependent(dependent: Dependent) {
        super.removeDependent(dependent)

        if (dependents.isEmpty()) {
            valid = false

            for (otherDependency in otherDependencies) {
                otherDependency.removeDependent(this)
            }

            list.removeDependent(this)
        }
    }

    override fun dependencyInvalidated(dependency: Dependency<*>) {
        valid = false

        if (dependency !== list) {
            otherDependencyInvalidated = true
        }

        emitDependencyInvalidated()
    }
}
