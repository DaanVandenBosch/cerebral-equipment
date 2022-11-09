package equipment.cerebral.cell.test

import equipment.cerebral.cell.list.ListCell
import kotlin.test.assertEquals

fun <E> assertListCellEquals(expected: List<E>, actual: ListCell<E>) {
    assertEquals(expected.size, actual.size.value)
    assertEquals(expected, actual.value)
}
