package equipment.cerebral.cell.disposable

class SimpleDisposable(
    private val dispose: () -> Unit,
) : TrackedDisposable() {
    override fun dispose() {
        // Use invoke to avoid calling the dispose method instead of the dispose property.
        dispose.invoke()
        super.dispose()
    }
}
