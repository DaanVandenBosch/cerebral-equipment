package equipment.cerebral.cell.list

import equipment.cerebral.cell.Cell
import equipment.cerebral.cell.CellTests
import equipment.cerebral.cell.ImmutableCell

/**
 * In these tests, the direct list cell dependency of the [ListElementsDependentCell] changes, while
 * its elements don't change.
 */
class ListElementsDependentCellListCellChangesTests : CellTests {

    override fun createProvider() = object : CellTests.Provider {
        // The direct dependency of the list under test changes, its elements are immutable.
        private val directDependency: SimpleListCell<Cell<Int>> =
            SimpleListCell(mutableListOf(ImmutableCell(1), ImmutableCell(2), ImmutableCell(3)))

        override val cell =
            ListElementsDependentCell(directDependency) { arrayOf(it) }

        override fun changeValue() {
            directDependency.add(ImmutableCell(directDependency.value.size + 1))
        }
    }
}
