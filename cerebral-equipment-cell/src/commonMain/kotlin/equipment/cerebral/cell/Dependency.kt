package equipment.cerebral.cell

/**
 * This interface is not meant to be implemented by typical application code.
 */
interface Dependency<out T> {
    /**
     * This method is not meant to be called from typical application code. Usually you'll want to
     * use [Cell.observeChange].
     */
    fun addDependent(dependent: Dependent)

    /**
     * This method is not meant to be called from typical application code.
     */
    fun removeDependent(dependent: Dependent)
}
