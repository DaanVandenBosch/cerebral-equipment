package equipment.cerebral.cell.list

import equipment.cerebral.cell.disposable.DisposableTracking
import equipment.cerebral.cell.observe
import equipment.cerebral.cell.test.CellTestSuite
import kotlin.test.Test
import kotlin.test.assertEquals

class ImmutableListCellTests : CellTestSuite {
    /**
     * As an optimization we simply ignore any observers and return a singleton Nop disposable.
     */
    @Test
    fun observing_it_never_creates_leaks() = test {
        val listCell = ImmutableListCell(listOf(1, 2, 3))

        DisposableTracking.checkNoLeaks {
            // We never call dispose on the returned disposables.
            listCell.observeChange {}
        }
    }

    @Test
    fun observe_calls_the_observer_once() = test {
        val listCell = ImmutableListCell(listOf(1, 2, 3))
        var calls = 0

        listCell.observe { calls++ }

        assertEquals(1, calls)
    }
}
