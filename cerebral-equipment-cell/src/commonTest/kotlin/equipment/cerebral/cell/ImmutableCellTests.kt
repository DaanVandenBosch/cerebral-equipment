package equipment.cerebral.cell

import equipment.cerebral.cell.disposable.DisposableTracking
import equipment.cerebral.cell.test.CellTestSuite
import kotlin.test.Test
import kotlin.test.assertEquals

class ImmutableCellTests : CellTestSuite {
    /**
     * As an optimization we simply ignore any observers and return a singleton Nop disposable.
     */
    @Test
    fun observing_it_never_creates_leaks() = test {
        val cell = ImmutableCell("test value")

        DisposableTracking.checkNoLeaks {
            // We never call dispose on the returned disposable.
            cell.observeChange {}
        }
    }

    @Test
    fun observe_calls_the_observer_once() = test {
        val cell = ImmutableCell("test value")
        var calls = 0

        cell.observe { calls++ }

        assertEquals(1, calls)
    }
}
