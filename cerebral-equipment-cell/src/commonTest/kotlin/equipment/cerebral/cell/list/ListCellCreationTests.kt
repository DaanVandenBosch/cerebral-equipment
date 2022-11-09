package equipment.cerebral.cell.list

import equipment.cerebral.cell.SimpleCell
import equipment.cerebral.cell.test.CellTestSuite
import equipment.cerebral.cell.test.assertListCellEquals
import kotlin.test.Test

class ListCellCreationTests : CellTestSuite {
    @Test
    fun test_flatMapToList() = test {
        val cell = SimpleCell(SimpleListCell(mutableListOf(1, 2, 3, 4, 5)))

        val mapped = cell.flatMapToList { it }

        assertListCellEquals(listOf(1, 2, 3, 4, 5), mapped)
    }
}
