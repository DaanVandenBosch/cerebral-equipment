package equipment.cerebral.cell

import equipment.cerebral.cell.test.CellTestSuite
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Test suite for all [Cell] implementations. There is a subclass of this suite for every [Cell]
 * implementation.
 */
interface CellTests : CellTestSuite {
    fun createProvider(): Provider

    /**
     * Tests low level [Dependency] implementation.
     */
    @Test
    fun correctly_emits_invalidation_notifications_to_its_dependents() = test {
        val p = createProvider()
        var dependencyInvalidatedCalled: Boolean

        p.cell.addDependent(object : Dependent {
            override fun dependencyInvalidated(dependency: Dependency<*>) {
                assertEquals(p.cell, dependency)
                dependencyInvalidatedCalled = true
            }
        })

        repeat(5) { index ->
            dependencyInvalidatedCalled = false

            p.changeValue()

            assertTrue(dependencyInvalidatedCalled, "repetition $index")
        }
    }

    @Test
    fun value_is_accessible_without_observers() = test {
        val p = createProvider()

        // We literally just test that accessing the value property doesn't throw or return null.
        assertNotNull(p.cell.value)
    }

    @Test
    fun value_is_accessible_with_observers() = test {
        val p = createProvider()

        disposer.add(p.cell.observeChange {})

        // We literally just test that accessing the value property doesn't throw or return null.
        assertNotNull(p.cell.value)
    }

    @Test
    fun calls_observers_when_value_changes() = test {
        val p = createProvider()
        var changes = 0

        disposer.add(
            p.cell.observeChange {
                changes++
            }
        )

        p.changeValue()

        assertEquals(1, changes)

        p.changeValue()
        p.changeValue()
        p.changeValue()

        assertEquals(4, changes)
    }

    @Test
    fun does_not_call_observers_after_they_are_disposed() = test {
        val p = createProvider()
        var changes = 0

        val observer = p.cell.observeChange {
            changes++
        }

        p.changeValue()

        assertEquals(1, changes)

        observer.dispose()

        p.changeValue()
        p.changeValue()
        p.changeValue()

        assertEquals(1, changes)
    }

    @Test
    fun does_not_call_observers_until_changed() = test {
        val p = createProvider()

        var observedValue: Any? = null

        disposer.add(p.cell.observeChange { newValue ->
            observedValue = newValue
        })

        assertNull(observedValue)

        p.changeValue()

        assertNotNull(observedValue)
    }

    @Test
    fun calls_observers_with_correct_value() = test {
        val p = createProvider()

        var prevValue: Snapshot?
        var observedValue: Snapshot? = null

        disposer.add(p.cell.observeChange { newValue ->
            assertNull(observedValue)
            observedValue = newValue.snapshot()
        })

        repeat(3) {
            prevValue = observedValue
            observedValue = null

            p.changeValue()

            // We should have observed a value, it should be different from the previous value, and
            // it should be equal to the cell's current value.
            assertNotNull(observedValue)
            assertNotEquals(prevValue, observedValue)
            assertEquals(p.cell.value.snapshot(), observedValue)
        }
    }

    /**
     * [Cell.value] should correctly reflect changes even when the [Cell] has no observers.
     * Typically this means that the cell's value is not updated in real time, only when it is
     * queried.
     */
    @Test
    fun reflects_changes_without_observers() = test {
        val p = createProvider()

        var old: Snapshot = p.cell.value.snapshot()

        repeat(5) {
            // Value should change when requested.
            p.changeValue()

            val new = p.cell.value.snapshot()

            assertNotEquals(old, new)

            // Value should not change when not requested since the last access.
            assertEquals(new, p.cell.value.snapshot())

            old = new
        }
    }

    //
    // CellUtils Tests
    //

    @Test
    fun propagates_changes_to_observers() = test {
        val p = createProvider()
        var changes = 0

        disposer.add(p.cell.observe {
            changes++
        })

        p.changeValue()

        assertEquals(2, changes)
    }

    @Test
    fun propagates_changes_to_mapped_cell() = test {
        val p = createProvider()
        val mapped = p.cell.map { it.snapshot() }
        val initialValue = mapped.value

        var observedValue: Snapshot? = null

        disposer.add(mapped.observeChange { newValue ->
            assertNull(observedValue)
            observedValue = newValue
        })

        p.changeValue()

        assertNotEquals(initialValue, mapped.value)
        assertEquals(mapped.value, observedValue)
    }

    @Test
    fun propagates_changes_to_flat_mapped_cell() = test {
        val p = createProvider()

        val mapped = p.cell.flatMap { ImmutableCell(it.snapshot()) }
        val initialValue = mapped.value

        var observedValue: Snapshot? = null

        disposer.add(mapped.observeChange { newValue ->
            assertNull(observedValue)
            observedValue = newValue
        })

        p.changeValue()

        assertNotEquals(initialValue, mapped.value)
        assertEquals(mapped.value, observedValue)
    }

    //
    // Mutation tests.
    //

    @Test
    fun changes_during_a_mutation_are_deferred() = test {
        val p = createProvider()
        var changes = 0

        disposer.add(
            p.cell.observeChange {
                changes++
            }
        )

        // Repeat 3 times to check that temporary state is always reset.
        repeat(3) {
            changes = 0

            mutate {
                repeat(5) {
                    p.changeValue()

                    // Change should be deferred until this mutation finishes.
                    assertEquals(0, changes)
                }
            }

            // All changes to the same cell should be collapsed to a single change.
            assertEquals(1, changes)
        }
    }

    /**
     * When two dependencies of an observer are changed in a single mutation, the observer is called
     * just once.
     */
    @Test
    fun changing_two_dependencies_during_a_mutation_results_in_one_callback_call() = test {
        val p1 = createProvider()
        val p2 = createProvider()
        var callbackCalled = 0

        disposer.add(
            CallbackObserver(p1.cell, p2.cell) { callbackCalled++ }
        )

        // Repeat 3 times to check that temporary state is always reset.
        repeat(3) {
            callbackCalled = 0

            mutate {
                p1.changeValue()
                p2.changeValue()

                // Change should be deferred until this mutation finishes.
                assertEquals(0, callbackCalled)
            }

            // All changes should result in a single callback call.
            assertEquals(1, callbackCalled)
        }
    }

    @Test
    fun value_can_be_accessed_during_a_mutation() = test {
        val p = createProvider()

        var observedValue: Snapshot? = null

        disposer.add(
            p.cell.observeChange { newValue ->
                // Change will be observed exactly once every loop iteration.
                assertNull(observedValue)
                observedValue = newValue.snapshot()
            }
        )

        // Repeat 3 times to check that temporary state is always reset.
        repeat(3) {
            observedValue = null

            val v1 = p.cell.value.snapshot()
            val v3: Snapshot

            mutate {
                val v2 = p.cell.value.snapshot()

                assertEquals(v1, v2)

                p.changeValue()
                v3 = p.cell.value.snapshot()

                assertNotEquals(v2, v3)

                p.changeValue()

                assertNull(observedValue)
            }

            val v4 = p.cell.value.snapshot()

            assertNotEquals(v3, v4)
            assertEquals(v4, observedValue)
        }
    }

    @Test
    fun mutations_can_be_nested() = test {
        // 3 Cells.
        val ps = Array(3) { createProvider() }
        val observedChanges = IntArray(ps.size)

        // Observe each cell.
        for (idx in ps.indices) {
            disposer.add(
                ps[idx].cell.observeChange {
                    assertEquals(0, observedChanges[idx])
                    observedChanges[idx]++
                }
            )
        }

        mutate {
            ps[0].changeValue()

            repeat(3) {
                mutate {
                    ps[1].changeValue()

                    mutate {
                        ps[2].changeValue()
                    }

                    assertTrue(observedChanges.all { it == 0 })
                }

                assertTrue(observedChanges.all { it == 0 })
            }
        }

        // At this point all 3 observers should be called exactly once.
        assertTrue(observedChanges.all { it == 1 })
    }

    interface Provider {
        val cell: Cell<Any>

        /**
         * Makes [cell]'s value change.
         */
        fun changeValue()
    }
}

/** See [snapshot]. */
typealias Snapshot = String

/**
 * We use toString to create "snapshots" of values throughout the tests. Most of the time cells will
 * actually have a new value after changing, but this is not always the case with more complex cells
 * or cells that point to complex values. So instead of keeping references to values and comparing
 * them with == (or using e.g. assertEquals), we compare snapshots.
 *
 * This of course assumes that all used values have sensible toString implementations.
 */
fun Any?.snapshot(): Snapshot = toString()
