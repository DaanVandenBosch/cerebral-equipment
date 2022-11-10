package equipment.cerebral.cell

internal class SimpleCell<T>(value: T) : AbstractCell<T>(), MutableCell<T> {
    override var value: T = value
        set(value) {
            if (value != field) {
                applyChange {
                    field = value
                    lastChanged = MutationManager.currentMutationId
                }
            }
        }

    override var lastChanged: Long = -1
        private set
}
