package equipment.cerebral.cell

/**
 * In these tests the direct dependency of the [FlatteningDependentCell] changes.
 */
@Suppress("unused")
class FlatteningDependentCellDirectDependencyChangesTests : CellTests {
    override fun createProvider() = object : CellTests.Provider {
        // The transitive dependency can't change.
        val transitiveDependency = ImmutableCell(5)

        // The direct dependency of the cell under test can change.
        val directDependency = SimpleCell(transitiveDependency)

        override val cell =
            FlatteningDependentCell(directDependency) { directDependency.value }

        override fun changeValue() {
            // Update the direct dependency.
            val oldTransitiveDependency = directDependency.value
            directDependency.value = ImmutableCell(oldTransitiveDependency.value + 5)
        }
    }
}
