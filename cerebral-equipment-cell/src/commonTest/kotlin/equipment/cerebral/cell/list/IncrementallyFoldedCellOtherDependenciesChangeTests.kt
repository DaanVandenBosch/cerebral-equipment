package equipment.cerebral.cell.list

import equipment.cerebral.cell.Cell
import equipment.cerebral.cell.CellTests
import equipment.cerebral.cell.CellWithDependenciesTests
import equipment.cerebral.cell.SimpleCell

/**
 * In these tests the other dependencies of the [IncrementallyFoldedCell] change.
 */
@Suppress("unused")
class IncrementallyFoldedCellOtherDependenciesChangeTests : CellWithDependenciesTests {

    override fun createProvider() = object : CellTests.Provider {
        private val otherDependencyCell = SimpleCell(1)

        override val cell =
            IncrementallyFoldedCell(
                // The list cell can't change.
                list = ImmutableListCell(listOf(1, 2, 3, 4, 5)),
                // The other dependency can change.
                otherDependencies = arrayOf(otherDependencyCell),
                computeValue = { it.sum() * otherDependencyCell.value },
                updateValue = { oldValue, change ->
                    val sum = change.removed.sum() + change.inserted.sum()
                    oldValue - sum * otherDependencyCell.value
                }
            )

        override fun changeValue() {
            otherDependencyCell.value += 1
        }
    }

    override fun createWithDependencies(
        dependency1: Cell<Int>,
        dependency2: Cell<Int>,
        dependency3: Cell<Int>,
    ): Cell<Any> =
        IncrementallyFoldedCell(
            // The list cell can't change.
            list = ImmutableListCell(listOf(1, 2, 3, 4, 5)),
            // The other dependencies can change.
            otherDependencies = arrayOf(dependency1, dependency2, dependency3),
            computeValue = { it.sum() + dependency1.value + dependency2.value + dependency3.value },
            updateValue = { oldValue, change ->
                oldValue - change.removed.sum() + change.inserted.sum()
            }
        )
}
