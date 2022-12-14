package equipment.cerebral.cell.list

import equipment.cerebral.cell.Cell
import equipment.cerebral.cell.CellWithDependenciesTests
import equipment.cerebral.cell.cell

/**
 * In these tests the list dependency of the [SimpleListCell] changes and the predicate
 * dependency does not.
 */
@Suppress("unused")
class SimpleFilteredListCellListDependencyChangesTests :
    ListCellTests, CellWithDependenciesTests {

    override fun createListProvider(empty: Boolean) = object : ListCellTests.Provider {
        private val dependencyCell =
            SimpleListCell(if (empty) mutableListOf(5) else mutableListOf(5, 10))

        override val cell = SimpleFilteredListCell(
            list = dependencyCell,
            predicate = cell { it % 2 == 0 },
        )

        override fun addElement() {
            dependencyCell.add(4)
        }
    }

    override fun createWithDependencies(
        dependency1: Cell<Int>,
        dependency2: Cell<Int>,
        dependency3: Cell<Int>,
    ): Cell<Any> =
        SimpleFilteredListCell(
            list = mapToList(dependency1, dependency2, dependency3) { value1, value2, value3 ->
                listOf(value1, value2, value3)
            },
            predicate = cell { it % 2 == 0 },
        )
}
