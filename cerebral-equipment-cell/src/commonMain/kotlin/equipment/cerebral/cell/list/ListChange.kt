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
    val newSize: Int get() = prevSize - removed.size + inserted.size

    /** True when this change resulted in the removal of all previous elements from the list. */
    val allRemoved: Boolean get() = removed.size == prevSize

    /**
     * True when the amount of removals and insertions is greater or equal to the list's new size.
     * This can often be used to optimize dependency updates.
     */
    val largeChange: Boolean get() = removed.size + inserted.size >= newSize

    override fun toString(): String =
        "ListChange(index=$index, prevSize=$prevSize, removed=$removed, inserted=$inserted)"
}
