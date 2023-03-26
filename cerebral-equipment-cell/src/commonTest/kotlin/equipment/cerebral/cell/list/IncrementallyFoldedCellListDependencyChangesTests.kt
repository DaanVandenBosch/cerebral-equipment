package equipment.cerebral.cell.list

import equipment.cerebral.cell.CellTests
import equipment.cerebral.cell.ImmutableCell

/**
 * In these tests the list dependency of the [IncrementallyFoldedCell] changes.
 */
@Suppress("unused")
class IncrementallyFoldedCellListDependencyChangesTests : CellTests {

    override fun createProvider() = object : CellTests.Provider {
        // The list cell changes.
        private val listCell = SimpleListCell<Int>(mutableListOf())

        override val cell =
            IncrementallyFoldedCell(
                list = listCell,
                // No other dependencies that can change.
                otherDependencies = arrayOf(ImmutableCell("immutable"), ImmutableCell("immutable")),
                computeValue = { it.sum() },
                updateValue = { oldValue, change ->
                    oldValue - change.removed.sum() + change.inserted.sum()
                }
            )

        override fun changeValue() {
            listCell.add(1)
        }
    }
}
