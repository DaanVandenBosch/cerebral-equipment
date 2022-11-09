package equipment.cerebral.cell

/**
 * Remove [n] elements at [startIndex].
 */
fun <E> MutableList<E>.removeAt(startIndex: Int, n: Int) {
    repeat(n) { removeAt(startIndex) }
}

/**
 * Replace [amount] elements at [startIndex] with [elements].
 */
fun <E> MutableList<E>.splice(startIndex: Int, amount: Int, elements: Iterable<E>) {
    removeAt(startIndex, amount)

    var i = startIndex

    for (element in elements) {
        add(i++, element)
    }
}
