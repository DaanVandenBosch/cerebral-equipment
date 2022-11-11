package equipment.cerebral.cell.list

import equipment.cerebral.cell.Cell
import equipment.cerebral.cell.ImmutableCell
import equipment.cerebral.cell.MutationManager
import equipment.cerebral.cell.SimpleCell
import equipment.cerebral.cell.mutate
import equipment.cerebral.cell.test.CellTestSuite
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests that apply to all filtered list implementations.
 */
interface AbstractFilteredListCellTests : CellTestSuite {
    fun <E> createFilteredListCell(list: ListCell<E>, predicate: Cell<(E) -> Boolean>): ListCell<E>

    @Test
    fun contains_only_values_that_match_the_predicate() = test {
        val dep = SimpleListCell(mutableListOf("a", "b"))
        val list = createFilteredListCell(dep, predicate = ImmutableCell { 'a' in it })

        assertEquals(1, list.value.size)
        assertEquals("a", list.value[0])

        dep.add("foo")
        dep.add("bar")

        assertEquals(2, list.value.size)
        assertEquals("a", list.value[0])
        assertEquals("bar", list.value[1])

        dep.add("quux")
        dep.add("qaax")

        assertEquals(3, list.value.size)
        assertEquals("a", list.value[0])
        assertEquals("bar", list.value[1])
        assertEquals("qaax", list.value[2])
    }

    @Test
    fun only_calls_observers_when_changed() = test {
        val dep = SimpleListCell<Int>(mutableListOf())
        val list = createFilteredListCell(dep, predicate = ImmutableCell { it % 2 == 0 })
        var changes = 0

        disposer.add(list.observeChange {
            changes++
        })

        dep.add(1)
        dep.add(3)
        dep.add(5)

        assertEquals(0, changes)

        dep.add(0)
        dep.add(2)
        dep.add(4)

        assertEquals(3, changes)
    }

    @Test
    fun calls_observers_with_correct_change_list() = test {
        val dep = SimpleListCell<Int>(mutableListOf())
        val list = createFilteredListCell(dep, predicate = ImmutableCell { it % 2 == 0 })
        var changes: List<ListChange<Int>>? = null

        disposer.add(list.observeChange {
            assertNull(changes)
            changes = list.changes.toList()
        })

        run {
            dep.replaceAll(listOf(1, 2, 3, 4, 5))

            val cs = changes
            assertNotNull(cs)
            assertEquals(1, cs.size)

            val c = cs.first()
            assertEquals(0, c.index)
            assertEquals(0, c.removed.size)
            assertEquals(2, c.inserted.size)
            assertEquals(2, c.inserted[0])
            assertEquals(4, c.inserted[1])
        }

        changes = null

        run {
            dep.splice(2, 2, 10)

            val cs = changes
            assertNotNull(cs)
            assertEquals(1, cs.size)

            val c = cs.first()
            assertEquals(1, c.index)
            assertEquals(1, c.removed.size)
            assertEquals(4, c.removed[0])
            assertEquals(1, c.inserted.size)
            assertEquals(10, c.inserted[0])
        }
    }

    @Test
    fun value_changes_and_observers_are_called_when_predicate_changes() = test {
        val predicate: SimpleCell<(Int) -> Boolean> = SimpleCell { it % 2 == 0 }
        val list = createFilteredListCell(ImmutableListCell(listOf(1, 2, 3, 4, 5)), predicate)
        var changes: List<ListChange<Int>>? = null

        disposer.add(list.observeChange {
            assertNull(changes)
            changes = list.changes.toList()
        })

        run {
            // Change predicate.
            predicate.value = { it % 2 == 1 }

            // Value changes.
            assertEquals(listOf(1, 3, 5), list.value)

            // List changes where created.
            val cs = changes
            assertNotNull(cs)
            assertEquals(1, cs.size)

            val c = cs.first()
            assertEquals(0, c.index)
            assertEquals(listOf(2, 4), c.removed)
            assertEquals(listOf(1, 3, 5), c.inserted)
        }

        changes = null

        run {
            // Change predicate.
            predicate.value = { it % 2 == 0 }

            // Value changes.
            assertEquals(listOf(2, 4), list.value)

            // List changes where created.
            val cs = changes
            assertNotNull(cs)
            assertEquals(1, cs.size)

            val c = cs.first()
            assertEquals(0, c.index)
            assertEquals(listOf(1, 3, 5), c.removed)
            assertEquals(listOf(2, 4), c.inserted)
        }
    }

    @Test
    fun observers_are_called_correctly_when_multiple_changes_happen_at_once() = test {
        val dependency = object : AbstractListCell<Int>() {
            private val elements: MutableList<Int> = mutableListOf()
            override val value: List<Int> get() = elements
            override var lastChanged: Long = -1
                private set
            override var changes: List<ListChange<Int>> = emptyList()
                private set

            fun makeChanges(newElements: List<Int>) {
                applyChange {
                    val changes = mutableListOf<ListChange<Int>>()

                    for (newElement in newElements) {
                        changes.add(
                            ListChange(
                                index = elements.size,
                                prevSize = elements.size,
                                removed = emptyList(),
                                inserted = listOf(newElement),
                            )
                        )
                        elements.add(newElement)
                    }

                    lastChanged = MutationManager.currentMutationId
                    this.changes = changes
                }
            }
        }

        val list = createFilteredListCell(dependency, ImmutableCell { true })
        var value: List<Int>? = null
        var changes: List<ListChange<Int>>? = null

        disposer.add(list.observeChange {
            assertNull(value)
            assertNull(changes)
            value = list.value.toList()
            changes = list.changes.toList()
        })

        for (i in 1..3) {
            value = null
            changes = null

            // Make two changes at once everytime.
            val changeEl0 = i * 13
            val changeEl1 = i * 17
            val changesEls = listOf(changeEl0, changeEl1)
            val oldList = list.value.toList()

            dependency.makeChanges(changesEls)

            // These checks are very implementation-specific. At some point the filtered list might,
            // for example, create a single change instead of two changes and then this test will
            // incorrectly fail.
            val v = value
            assertNotNull(v)
            assertEquals(oldList + changesEls, v)

            val cs = changes
            assertNotNull(cs)
            assertEquals(2, cs.size)

            val c0 = cs[0]
            assertEquals(oldList.size, c0.index)
            assertEquals(oldList.size, c0.prevSize)
            assertTrue(c0.removed.isEmpty())
            assertEquals(listOf(changeEl0), c0.inserted)

            val c1 = cs[1]
            assertEquals(oldList.size + 1, c1.index)
            assertEquals(oldList.size + 1, c1.prevSize)
            assertTrue(c1.removed.isEmpty())
            assertEquals(listOf(changeEl1), c1.inserted)
        }
    }

    @Test
    fun observers_are_called_correctly_when_dependency_contains_same_element_twice() = test {
        val x = "x"
        val y = "y"
        val z = "z"
        val dependency = SimpleListCell(mutableListOf(x, y, z, x, y, z))
        val list = createFilteredListCell(dependency, ImmutableCell { it != y })
        var changes: List<ListChange<String>>? = null

        disposer.add(list.observeChange {
            assertNull(changes)
            changes = list.changes
        })

        assertEquals(listOf(x, z, x, z), list.value)

        run {
            // Remove second x element.
            dependency.removeAt(3)

            // Value changes.
            assertEquals(listOf(x, z, z), list.value)

            // List changes where created.
            val cs = changes
            assertNotNull(cs)
            assertEquals(1, cs.size)

            val c = cs.first()
            assertEquals(2, c.index)
            assertEquals(listOf(x), c.removed)
            assertTrue(c.inserted.isEmpty())
        }

        changes = null

        run {
            // Remove first x element.
            dependency.removeAt(0)

            // Value changes.
            assertEquals(listOf(z, z), list.value)

            // List changes where created.
            val cs = changes
            assertNotNull(cs)
            assertEquals(1, cs.size)

            val c = cs.first()
            assertEquals(0, c.index)
            assertEquals(listOf(x), c.removed)
            assertTrue(c.inserted.isEmpty())
        }

        changes = null

        run {
            // Remove second z element.
            dependency.removeAt(3)

            // Value changes.
            assertEquals(listOf(z), list.value)

            // List changes where created.
            val cs = changes
            assertNotNull(cs)
            assertEquals(1, cs.size)

            val c = cs.first()
            assertEquals(1, c.index)
            assertEquals(listOf(z), c.removed)
            assertTrue(c.inserted.isEmpty())
        }
    }

    /**
     * This tests the short-circuit path where a filtered list's predicate changes.
     */
    @Test
    fun dependent_filtered_list_changes_correctly_when_predicate_changes() = test {
        val list = mutableListCell(1, 2, 3, 4, 5, 6)
        val predicate: SimpleCell<(Int) -> Boolean> = SimpleCell { it % 2 == 0 }
        val filteredList = createFilteredListCell(list, predicate)
        val dependentList = filteredList.filtered { true }

        var observerCalled = false

        disposer.add(dependentList.observeChange {
            assertFalse(observerCalled)
            observerCalled = true
        })

        assertEquals(listOf(2, 4, 6), dependentList.value)

        mutate {
            // Trigger long path.
            list.add(10)
            dependentList.value

            // Trigger long path again.
            list.add(20)
            dependentList.value

            // Trigger short path.
            predicate.value = { it % 2 != 0 }
        }

        assertEquals(listOf(1, 3, 5), dependentList.value)

        assertEquals(3, dependentList.changes.size)

        val c0 = dependentList.changes[0]
        assertEquals(3, c0.index)
        assertEquals(3, c0.prevSize)
        assertEquals(emptyList(), c0.removed)
        assertEquals(listOf(10), c0.inserted)

        val c1 = dependentList.changes[1]
        assertEquals(4, c1.index)
        assertEquals(4, c1.prevSize)
        assertEquals(emptyList(), c1.removed)
        assertEquals(listOf(20), c1.inserted)

        val c2 = dependentList.changes[2]
        assertEquals(0, c2.index)
        assertEquals(5, c2.prevSize)
        assertEquals(listOf(2, 4, 6, 10, 20), c2.removed)
        assertEquals(listOf(1, 3, 5), c2.inserted)

        assertTrue(observerCalled)
    }
}
