package equipment.cerebral.cell.list

import equipment.cerebral.cell.SimpleCell
import equipment.cerebral.cell.test.CellTestSuite
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull

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

        var observedValue: List<SimpleCell<*>>? = null

        disposer.add(cell.observeChange {
            assertNull(observedValue)
            observedValue = it
        })

        // The cell should not call observers when an old element is changed.
        run {
            val removed = list.removeAt(1)
            observedValue = null

            removed.value += "-1"

            assertNull(observedValue)
        }

        // The cell should call observers when any of the current elements are changed.
        list.add(SimpleCell("d"))

        for (element in list.value) {
            observedValue = null

            element.value += "-2"

            val e = observedValue
            assertNotNull(e)
        }
    }
}
