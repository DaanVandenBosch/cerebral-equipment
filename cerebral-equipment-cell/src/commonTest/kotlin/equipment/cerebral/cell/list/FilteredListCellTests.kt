package equipment.cerebral.cell.list

import equipment.cerebral.cell.Cell
import equipment.cerebral.cell.cell
import equipment.cerebral.cell.map

// IMPROVE: A test suite that tests FilteredListCell while all 3 types of dependencies are changing.
/**
 * Standard tests are done by [FilteredListCellListDependencyChangesTests],
 * [FilteredListCellPredicateDependencyChangesTests] and
 * [FilteredListCellPredicateResultDependenciesChangeTests].
 */
@Suppress("unused")
class FilteredListCellTests : AbstractFilteredListCellTests {
    override fun <E> createFilteredListCell(
        list: ListCell<E>,
        predicate: Cell<(E) -> Boolean>,
    ): ListCell<E> =
        FilteredListCell(list, predicate.map { p -> { cell(p(it)) } })
}
