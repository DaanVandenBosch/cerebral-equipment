package equipment.cerebral.cell

/**
 * In these tests both the direct dependency and the transitive dependency of the
 * [FlatteningDependentCell] change.
 */
@Suppress("unused")
class FlatteningDependentCellDirectAndTransitiveDependencyChangeTests : CellTests {
    override fun createProvider() = Provider()

    class Provider : CellTests.Provider {
        // This cell is both the direct and transitive dependency.
        private val dependencyCell = SimpleCell('a')

        override val cell: Cell<Any> = FlatteningDependentCell(dependencyCell) { dependencyCell }

        override fun changeValue() {
            dependencyCell.value += 1
        }
    }
}
