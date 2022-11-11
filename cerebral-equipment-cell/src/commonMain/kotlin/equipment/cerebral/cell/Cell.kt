package equipment.cerebral.cell

import equipment.cerebral.cell.disposable.Disposable
import kotlin.reflect.KProperty

/**
 * A [value] that can change over time.
 */
interface Cell<out T> : Dependency<T> {
    /** The cell's current value. */
    val value: T

    /** The ID of the mutation during which this cell's [value] was last changed. */
    val lastChanged: Long

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T = value

    /**
     * [observer] will be called whenever this cell changes.
     */
    fun observeChange(observer: () -> Unit): Disposable
}
