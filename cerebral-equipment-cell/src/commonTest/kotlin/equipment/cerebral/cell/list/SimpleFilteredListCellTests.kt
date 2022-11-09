package equipment.cerebral.cell.list

import equipment.cerebral.cell.Cell

// IMPROVE: A test suite that tests SimpleFilteredListCell while both types of dependencies are
// changing.
/**
 * Standard tests are done by [SimpleFilteredListCellListDependencyEmitsTests] and
 * [SimpleFilteredListCellPredicateDependencyEmitsTests].
 */
@Suppress("unused")
class SimpleFilteredListCellTests : AbstractFilteredListCellTests {
    override fun <E> createFilteredListCell(
        list: ListCell<E>,
        predicate: Cell<(E) -> Boolean>,
    ): ListCell<E> =
        SimpleFilteredListCell(list, predicate)
}
