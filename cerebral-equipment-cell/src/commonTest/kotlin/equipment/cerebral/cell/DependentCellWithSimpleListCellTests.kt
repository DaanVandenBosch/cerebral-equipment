package equipment.cerebral.cell

import equipment.cerebral.cell.list.SimpleListCell

@Suppress("unused")
class DependentCellWithSimpleListCellTests : CellTests {
    override fun createProvider() = Provider()

    class Provider : CellTests.Provider {
        private val dependencyCell = SimpleListCell(mutableListOf("a", "b", "c"))

        override val cell: Cell<Any> = DependentCell(dependencyCell) { dependencyCell.value }

        override fun changeValue() {
            dependencyCell.add("x")
        }
    }
}
