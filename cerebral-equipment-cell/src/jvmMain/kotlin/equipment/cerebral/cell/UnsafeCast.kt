@file:JvmName("UnsafeCastJvm")

package equipment.cerebral.cell

@Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE")
actual inline fun <T> unsafeCast(value: Any?): T = value as T
