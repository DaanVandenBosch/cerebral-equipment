package equipment.cerebral.cell

import equipment.cerebral.cell.test.CellTestSuite
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MutationTests : CellTestSuite {
    @Test
    fun can_change_observed_cell_with_deferred_mutation() = test {
        val cell = mutableCell(0)
        var observedChanges = 0

        disposer.add(cell.observeChange {
            observedChanges++

            if (cell.value < 10) {
                // Changing the cell here would throw an exception because, while a cell is
                // changing, no other cell can change. Deferring the change is allowed though.
                mutateDeferred {
                    cell.value++
                }
            }
        })

        cell.value = 1

        assertEquals(10, observedChanges)
        assertEquals(10, cell.value)
    }

    /**
     * All deferred mutations happen at the end of the outer mutation.
     */
    @Test
    fun can_nest_deferred_mutations_in_regular_mutations() = test {
        val cell = mutableCell(0)
        var observerChanges = 0

        disposer.add(cell.observeChange {
            observerChanges++
        })

        mutate {
            mutateDeferred {
                cell.value = 3 // Happens third.
            }

            mutate {
                mutateDeferred {
                    cell.value = 4 // Happens fourth.
                }

                cell.value = 1 // Happens first.
            }

            cell.value = 2 // Happens second.
        }

        assertEquals(3, observerChanges)
        assertEquals(4, cell.value)
    }

    @Test
    fun exceptions_during_a_mutation_are_allowed() = test {
        val dependency = mutableCell(7)
        val dependent = dependency.map { 2 * it }

        var observerCalled = false

        disposer.add(dependent.observeChange {
            assertFalse(observerCalled)
            observerCalled = true
        })

        assertFails {
            mutate {
                dependency.value = 11
                throw TestException()
            }
        }

        // The change to dependency is still propagated because it happened before the exception.
        assertEquals(22, dependent.value)
        assertTrue(observerCalled)

        observerCalled = false

        // The mutation machinery is still in a valid state.
        mutate {
            dependency.value = 13
        }

        assertEquals(26, dependent.value)
        assertTrue(observerCalled)
    }

    private class TestException : Exception()
}
