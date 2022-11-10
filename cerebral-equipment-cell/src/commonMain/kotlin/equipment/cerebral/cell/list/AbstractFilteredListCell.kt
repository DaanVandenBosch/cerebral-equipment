package equipment.cerebral.cell.list

import equipment.cerebral.cell.Dependency
import equipment.cerebral.cell.Dependent
import equipment.cerebral.cell.MutationManager

internal abstract class AbstractFilteredListCell<E>(
    protected val list: ListCell<E>,
) : AbstractListCell<E>(), Dependent {

    /** Set during a change wave when [list] changes. */
    private var listInvalidated = false

    private var listChangeIndex = 0

    /** Set during a change wave when [predicateDependency] changes. */
    private var predicateInvalidated = false

    private var valid = false

    protected val elements = mutableListOf<E>()

    protected abstract val predicateDependency: Dependency<*>

    final override val value: List<E>
        get() {
            computeValueAndChanges()
            return elements
        }

    private var _lastChanged: Long = -1
    final override val lastChanged: Long
        get() {
            computeValueAndChanges()
            return _lastChanged
        }

    private val _changes: MutableList<ListChange<E>> = mutableListOf()
    final override val changes: List<ListChange<E>>
        get() {
            computeValueAndChanges()
            return _changes
        }

    private fun computeValueAndChanges() {
        if (!valid) {
            val filteredChanges = _changes

            // Only clear changes once during each mutation.
            val currentMutationId = MutationManager.currentMutationId

            if (_lastChanged != currentMutationId) {
                listChangeIndex = 0
                filteredChanges.clear()
            }

            val hasDependents = dependents.isNotEmpty()

            if (predicateInvalidated || !hasDependents) {
                // Simply assume the entire list changes and recompute.
                val removed = elements.toList()

                ignoreOtherChanges()
                recompute()

                filteredChanges.add(
                    ListChange(index = 0, prevSize = removed.size, removed, elements),
                )

                _lastChanged = currentMutationId
            } else {
                val listChanges = list.changes

                if (listInvalidated) {
                    for (change in listChanges.listIterator(listChangeIndex)) {
                        val prevSize = elements.size
                        // Map the incoming change index to an index into our own elements list.
                        var elementIndex = prevSize

                        // IMPROVE: Avoid this loop by storing the index where an element "would" be
                        // if it passed the predicate?
                        for (index in change.index..maxDepIndex()) {
                            val i = mapIndex(index)

                            if (i != -1) {
                                elementIndex = i
                                break
                            }
                        }

                        // Process removals.
                        val removed = mutableListOf<E>()

                        for (element in change.removed) {
                            val index = removeIndexMapping(change.index)

                            if (index != -1) {
                                elements.removeAt(elementIndex)
                                removed.add(element)
                            }
                        }

                        // Process insertions.
                        val inserted = mutableListOf<E>()
                        var insertionIndex = elementIndex

                        for ((i, element) in change.inserted.withIndex()) {
                            if (applyPredicate(element)) {
                                insertIndexMapping(change.index + i, insertionIndex, element)
                                elements.add(insertionIndex, element)
                                inserted.add(element)
                                insertionIndex++
                            } else {
                                insertIndexMapping(change.index + i, -1, element)
                            }
                        }

                        // Shift mapped indices by a certain amount. This amount can be
                        // positive, negative or zero.
                        val diff = inserted.size - removed.size

                        if (diff != 0) {
                            // Indices before the change index stay the same. Newly inserted
                            // indices are already correct. So we only need to shift everything
                            // after the new indices.
                            val startIndex = change.index + change.inserted.size

                            for (index in startIndex..maxDepIndex()) {
                                shiftIndexMapping(index, diff)
                            }
                        }

                        // Add a list change if something actually changed.
                        if (removed.isNotEmpty() || inserted.isNotEmpty()) {
                            filteredChanges.add(
                                ListChange(
                                    elementIndex,
                                    prevSize,
                                    removed,
                                    inserted,
                                )
                            )
                        }
                    }

                    listChangeIndex = listChanges.size
                }

                processOtherChanges(filteredChanges)

                if (filteredChanges.isNotEmpty()) {
                    _lastChanged = currentMutationId
                }
            }

            // Reset for next change wave.
            listInvalidated = false
            predicateInvalidated = false
            // We stay invalid if we have no dependents to ensure our value is always recomputed.
            valid = hasDependents
        }

        resetChangeWaveData()
    }

    override fun addDependent(dependent: Dependent) {
        super.addDependent(dependent)

        if (dependents.size == 1) {
            list.addDependent(this)
            predicateDependency.addDependent(this)
            recompute()
        }
    }

    override fun removeDependent(dependent: Dependent) {
        super.removeDependent(dependent)

        if (dependents.isEmpty()) {
            valid = false
            predicateDependency.removeDependent(this)
            list.removeDependent(this)
        }
    }

    override fun dependencyInvalidated(dependency: Dependency<*>) {
        valid = false

        if (dependency === list) {
            listInvalidated = true
        } else if (dependency === predicateDependency) {
            predicateInvalidated = true
        } else {
            otherDependencyInvalidated(dependency)
        }

        emitDependencyInvalidated()
    }

    /** Called when a dependency that's neither [list] nor [predicateDependency] has changed. */
    protected abstract fun otherDependencyInvalidated(dependency: Dependency<*>)

    protected abstract fun ignoreOtherChanges()

    protected abstract fun processOtherChanges(filteredChanges: MutableList<ListChange<E>>)

    protected abstract fun applyPredicate(element: E): Boolean

    protected abstract fun maxDepIndex(): Int

    /**
     * Maps and index into [list] to an index into this list. Returns -1 if the given index does not
     * point to an element that passes the predicate, i.e. the element is not in this list.
     */
    protected abstract fun mapIndex(index: Int): Int

    /**
     * Removes the element at the given index into [list] from our mapping. Returns the previous
     * index into our list.
     */
    protected abstract fun removeIndexMapping(index: Int): Int

    protected abstract fun insertIndexMapping(depIndex: Int, localIndex: Int, element: E)

    /** Adds [shift] to the local index at [depIndex] if it's not -1. */
    protected abstract fun shiftIndexMapping(depIndex: Int, shift: Int)

    protected abstract fun recompute()

    protected abstract fun resetChangeWaveData()
}
