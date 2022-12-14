package equipment.cerebral.cell

@Suppress("unused")
class DelegatingCellTests : MutableCellTests<Int> {
    override fun createProvider() = object : MutableCellTests.Provider<Int> {
        private var v = 17

        override val cell = DelegatingCell({ v }, { v = it })

        override fun changeValue() {
            cell.value += 2
        }

        override fun createValue(): Int = v + 1
    }
}
