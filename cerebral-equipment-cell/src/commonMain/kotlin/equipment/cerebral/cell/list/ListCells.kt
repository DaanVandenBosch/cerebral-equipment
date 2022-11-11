@file:JvmName("ListCells")

package equipment.cerebral.cell.list

import equipment.cerebral.cell.Cell
import equipment.cerebral.cell.DependentCell
import equipment.cerebral.cell.ImmutableCell
import kotlin.jvm.JvmName

private val EMPTY_LIST_CELL = ImmutableListCell<Nothing>(emptyList())

/** Returns an immutable list cell containing [elements]. */
fun <E> listCell(vararg elements: E): ListCell<E> = ImmutableListCell(elements.toList())

/** Returns a list cell backed by [elements]. */
fun <E> listCell(elements: List<E>): ListCell<E> =
    ImmutableListCell(elements)

/** Returns a singleton empty immutable cell. */
fun <E> emptyListCell(): ListCell<E> = EMPTY_LIST_CELL

/** Returns a mutable list cell containing [elements]. */
fun <E> mutableListCell(vararg elements: E): MutableListCell<E> =
    SimpleListCell(mutableListOf(*elements))

/** Returns a mutable list cell initially backed by [elements]. */
fun <E> mutableListCell(elements: MutableList<E>): MutableListCell<E> =
    SimpleListCell(elements)

/**
 * Returns a cell that changes whenever this list cell is structurally changed or when its
 * individual elements change.
 *
 * @param extractCells Called on each element to determine which element changes should be observed.
 */
fun <E> ListCell<E>.dependingOnElements(
    extractCells: (element: E) -> Array<out Cell<*>>,
): Cell<List<E>> =
    ListElementsDependentCell(this, extractCells)

fun <E, R> ListCell<E>.listMap(transform: (E) -> R): ListCell<R> =
    DependentListCell(this) { value.map(transform) }

fun <E, R> ListCell<E>.fold(initialValue: R, operation: (R, E) -> R): Cell<R> =
    DependentCell(this) { value.fold(initialValue, operation) }

// IMPROVE: Don't loop over entire list on every change.
fun <E> ListCell<E>.all(predicate: (E) -> Boolean): Cell<Boolean> =
    DependentCell(this) { value.all(predicate) }

// IMPROVE: Don't loop over entire list on every change.
fun <E> ListCell<E>.any(predicate: (E) -> Boolean): Cell<Boolean> =
    DependentCell(this) { value.any(predicate) }

// IMPROVE: Don't loop over entire list on every change.
fun <E> ListCell<E>.count(predicate: (E) -> Boolean): Cell<Int> =
    DependentCell(this) { value.count(predicate) }

// IMPROVE: Don't loop over entire list on every change.
fun <E> ListCell<E>.sumOf(selector: (E) -> Int): Cell<Int> =
    DependentCell(this) { value.sumOf(selector) }

fun <E> ListCell<E>.filtered(predicate: (E) -> Boolean): ListCell<E> =
    SimpleFilteredListCell(this, ImmutableCell(predicate))

fun <E> ListCell<E>.filtered(predicate: Cell<(E) -> Boolean>): ListCell<E> =
    SimpleFilteredListCell(this, predicate)

fun <E> ListCell<E>.filteredCell(predicate: (E) -> Cell<Boolean>): ListCell<E> =
    FilteredListCell(this, ImmutableCell(predicate))

fun <E> ListCell<E>.filteredCell(predicate: Cell<(E) -> Cell<Boolean>>): ListCell<E> =
    FilteredListCell(this, predicate)

// IMPROVE: Only change when first element actually changes.
fun <E> ListCell<E>.first(): Cell<E> =
    DependentCell(this) { value.first() }

// IMPROVE: Only change when first element actually changes.
fun <E> ListCell<E>.firstOrNull(): Cell<E?> =
    DependentCell(this) { value.firstOrNull() }

fun <T, R> Cell<T>.mapToList(
    transform: (T) -> List<R>,
): ListCell<R> =
    DependentListCell(this) { transform(value) }

fun <T1, T2, R> mapToList(
    c1: Cell<T1>,
    c2: Cell<T2>,
    transform: (T1, T2) -> List<R>,
): ListCell<R> =
    DependentListCell(c1, c2) { transform(c1.value, c2.value) }

fun <T1, T2, T3, R> mapToList(
    c1: Cell<T1>,
    c2: Cell<T2>,
    c3: Cell<T3>,
    transform: (T1, T2, T3) -> List<R>,
): ListCell<R> =
    DependentListCell(c1, c2, c3) { transform(c1.value, c2.value, c3.value) }

fun <T, R> Cell<T>.flatMapToList(
    transform: (T) -> ListCell<R>,
): ListCell<R> =
    FlatteningDependentListCell(this) { transform(value) }

fun <T1, T2, R> flatMapToList(
    c1: Cell<T1>,
    c2: Cell<T2>,
    transform: (T1, T2) -> ListCell<R>,
): ListCell<R> =
    FlatteningDependentListCell(c1, c2) { transform(c1.value, c2.value) }

fun listCellToString(cell: ListCell<*>): String =
    buildString {
        append(cell::class.simpleName)
        append('[')
        cell.value.joinTo(this, limit = 20) {
            if (it === cell) "(this cell)" else it.toString()
        }
        append(']')
    }
