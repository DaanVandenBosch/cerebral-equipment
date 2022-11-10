package equipment.cerebral.cell

@Suppress("unused")
class SimpleCellTests : MutableCellTests<Int> {
    override fun createProvider() = object : MutableCellTests.Provider<Int> {
        override val cell = SimpleCell(1)

        override fun changeValue() {
            cell.value += 2
        }

        override fun createValue(): Int = cell.value + 1
    }
}
