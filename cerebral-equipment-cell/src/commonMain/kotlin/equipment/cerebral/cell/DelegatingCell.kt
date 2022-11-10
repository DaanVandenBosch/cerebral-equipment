package equipment.cerebral.cell

internal class DelegatingCell<T>(
    private val getter: () -> T,
    private val setter: (T) -> Unit,
) : AbstractCell<T>(), MutableCell<T> {
    override var value: T = getter()
        set(value) {
            setter(value)
            val newValue = getter()

            if (newValue != field) {
                applyChange {
                    field = newValue
                    lastChanged = MutationManager.currentMutationId
                }
            }
        }

    override var lastChanged: Long = -1
        private set
}
