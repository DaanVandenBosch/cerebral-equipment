package equipment.cerebral.cell.test

import equipment.cerebral.cell.disposable.DisposableTracking
import equipment.cerebral.cell.disposable.Disposer

interface CellTestSuite  {
    fun test(testBlock: TestContext.() -> Unit) {
        DisposableTracking.checkNoLeaks {
            val disposer = Disposer()

            TestContext(disposer).testBlock()

            disposer.dispose()
        }
    }
}

class TestContext(val disposer: Disposer)
