package equipment.cerebral.cell

/**
 * Similar to [DependentCell], except that this cell's [compute] returns a cell.
 */
internal class FlatteningDependentCell<T>(
    vararg dependencies: Cell<*>,
    compute: () -> Cell<T>,
) : AbstractFlatteningDependentCell<T, Cell<T>>(dependencies, compute) {
    override fun updateValueAndLastChanged(oldValue: T?, newValue: T) {
        valueInternal = newValue
        lastChangedInternal = MutationManager.currentMutationId
    }
}
