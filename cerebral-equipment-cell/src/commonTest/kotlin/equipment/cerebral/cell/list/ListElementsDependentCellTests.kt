package equipment.cerebral.cell.list

import equipment.cerebral.cell.SimpleCell
import equipment.cerebral.cell.test.CellTestSuite
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Standard tests are done by [ListElementsDependentCellElementChangesTests] and
 * [ListElementsDependentCellListCellChangesTests].
 */
class ListElementsDependentCellTests : CellTestSuite {
    @Test
    fun element_changes_are_correctly_propagated() = test {
        val list = SimpleListCell(
            mutableListOf(
                SimpleCell("a"),
                SimpleCell("b"),
                SimpleCell("c")
            )
        )

        val cell = ListElementsDependentCell(list) { arrayOf(it) }

        var observerCalled = false

        disposer.add(cell.observeChange {
            assertFalse(observerCalled)
            observerCalled = true
        })

        // The cell should not call observers when an old element is changed.
        run {
            val removed = list.removeAt(1)
            observerCalled = false

            removed.value += "-1"

            assertFalse(observerCalled)
        }

        // The cell should call observers when any of the current elements are changed.
        list.add(SimpleCell("d"))

        for (element in list.value) {
            observerCalled = false

            element.value += "-2"

            assertTrue(observerCalled)
        }
    }
}
