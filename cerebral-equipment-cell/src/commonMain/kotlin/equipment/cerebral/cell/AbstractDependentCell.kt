package equipment.cerebral.cell

internal abstract class AbstractDependentCell<T, Event : ChangeEvent<T>> :
    AbstractCell<T>(), Dependent {

    protected var valueInternal: T? = null
    final override val value: T
        get() {
            computeValueAndEvent()
            // We cast instead of asserting _value is non-null because T might actually be a
            // nullable type.
            return unsafeCast(valueInternal)
        }

    final override var changeEvent: Event? = null
        get() {
            computeValueAndEvent()
            return field
        }
        protected set

    protected abstract fun computeValueAndEvent()
}
