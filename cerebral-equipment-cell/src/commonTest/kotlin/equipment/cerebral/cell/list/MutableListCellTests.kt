package equipment.cerebral.cell.list

import equipment.cerebral.cell.MutableCellTests
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Test suite for all [MutableListCell] implementations. There is a subclass of this suite for every
 * [MutableListCell] implementation.
 */
interface MutableListCellTests<T : Any> : ListCellTests, MutableCellTests<List<T>> {
    override fun createProvider(): Provider<T>

    @Test
    fun add() = test {
        val p = createProvider()

        var observedChanges: List<ListChange<T>>? = null

        disposer.add(p.cell.observeListChange {
            assertNull(observedChanges)
            observedChanges = it
        })

        // Insert once.
        val v1 = p.createElement()
        p.cell.add(v1)

        run {
            assertEquals(1, p.cell.size.value)
            assertEquals(v1, p.cell[0])

            val cs = observedChanges
            assertNotNull(cs)
            assertEquals(1, cs.size)

            val c0 = cs[0]
            assertEquals(0, c0.index)
            assertTrue(c0.removed.isEmpty())
            assertEquals(1, c0.inserted.size)
            assertEquals(v1, c0.inserted[0])
        }

        // Insert a second time.
        observedChanges = null

        val v2 = p.createElement()
        p.cell.add(v2)

        run {
            assertEquals(2, p.cell.size.value)
            assertEquals(v1, p.cell[0])
            assertEquals(v2, p.cell[1])

            val cs = observedChanges
            assertNotNull(cs)
            assertEquals(1, cs.size)

            val c0 = cs[0]
            assertEquals(1, c0.index)
            assertTrue(c0.removed.isEmpty())
            assertEquals(1, c0.inserted.size)
            assertEquals(v2, c0.inserted[0])
        }

        // Insert at index.
        observedChanges = null

        val v3 = p.createElement()
        p.cell.add(1, v3)

        run {
            assertEquals(3, p.cell.size.value)
            assertEquals(v1, p.cell[0])
            assertEquals(v3, p.cell[1])
            assertEquals(v2, p.cell[2])

            val cs = observedChanges
            assertNotNull(cs)
            assertEquals(1, cs.size)

            val c0 = cs[0]
            assertEquals(1, c0.index)
            assertTrue(c0.removed.isEmpty())
            assertEquals(1, c0.inserted.size)
            assertEquals(v3, c0.inserted[0])
        }
    }

    interface Provider<T : Any> : ListCellTests.Provider, MutableCellTests.Provider<List<T>> {
        override val cell: MutableListCell<T>

        fun createElement(): T
    }
}
