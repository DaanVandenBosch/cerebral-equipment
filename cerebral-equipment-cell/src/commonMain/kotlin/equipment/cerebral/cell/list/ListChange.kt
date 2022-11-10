package equipment.cerebral.cell.list

/**
 * Represents a structural change to a list cell. E.g. an element is inserted or removed.
 */
class ListChange<out E>(
    val index: Int,
    val prevSize: Int,
    /** The elements that were removed from the list at [index]. */
    val removed: List<E>,
    /** The elements that were inserted into the list at [index]. */
    val inserted: List<E>,
) {
    /** True when this change resulted in the removal of all elements from the list. */
    val allRemoved: Boolean get() = removed.size == prevSize

    override fun toString(): String =
        "ListChange(index=$index, prevSize=$prevSize, removed=$removed, inserted=$inserted)"
}
