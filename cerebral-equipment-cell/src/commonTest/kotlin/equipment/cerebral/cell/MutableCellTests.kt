package equipment.cerebral.cell

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

interface MutableCellTests<T : Any> : CellTests {
    override fun createProvider(): Provider<T>

    @Test
    fun calls_observers_when_value_is_modified() = test {
        val p = createProvider()

        var observerCalled = false

        disposer.add(p.cell.observeChange {
            assertFalse(observerCalled)
            observerCalled = true
        })

        val newValue = p.createValue()
        p.cell.value = newValue

        assertEquals(newValue, p.cell.value)
        assertTrue(observerCalled)
    }

    interface Provider<T : Any> : CellTests.Provider {
        override val cell: MutableCell<T>

        /**
         * Returns a value that can be assigned to [cell] and that's different from
         * [cell]'s current and all previous values.
         */
        fun createValue(): T
    }
}
