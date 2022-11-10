package equipment.cerebral.cell

internal abstract class AbstractDependentCell<T> : AbstractCell<T>(), Dependent {

    protected var valueInternal: T? = null
    final override val value: T
        get() {
            computeValueAndLastChanged()
            // We cast instead of asserting _value is non-null because T might actually be a
            // nullable type.
            return unsafeCast(valueInternal)
        }

    protected var lastChangedInternal: Long = -1
    override val lastChanged: Long
        get() {
            computeValueAndLastChanged()
            return lastChangedInternal
        }

    protected abstract fun computeValueAndLastChanged()
}
