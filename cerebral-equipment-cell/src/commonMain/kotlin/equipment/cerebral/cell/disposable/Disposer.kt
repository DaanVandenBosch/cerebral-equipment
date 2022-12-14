package equipment.cerebral.cell.disposable

/**
 * Container for disposables that disposes the contained disposables when it is disposed.
 */
class Disposer(vararg disposables: Disposable) : TrackedDisposable() {
    private val disposables = ArrayList(disposables.asList())

    /**
     * The amount of held disposables.
     */
    val size: Int get() = disposables.size

    fun <T : Disposable> add(disposable: T): T {
        require(!disposed) { "Disposer already disposed." }

        disposables.add(disposable)
        return disposable
    }

    fun <T : Disposable> add(index: Int, disposable: T): T {
        require(!disposed) { "Disposer already disposed." }

        disposables.add(index, disposable)
        return disposable
    }

    /**
     * Add 0 or more disposables.
     */
    fun addAll(disposables: Iterable<Disposable>) {
        require(!disposed) { "Disposer already disposed." }

        this.disposables.addAll(disposables)
    }

    /**
     * Add 0 or more disposables.
     */
    fun addAll(vararg disposables: Disposable) {
        require(!disposed) { "Disposer already disposed." }

        this.disposables.addAll(disposables)
    }

    fun isEmpty(): Boolean = disposables.isEmpty()

    /**
     * Removes and by default disposes the given [disposable].
     */
    fun remove(disposable: Disposable, dispose: Boolean = true) {
        disposables.remove(disposable)
        if (dispose) disposable.dispose()
    }

    /**
     * Removes and disposes [amount] disposables at the given [index].
     */
    fun removeAt(index: Int, amount: Int = 1, dispose: Boolean = true) {
        repeat(amount) {
            val disposable = disposables.removeAt(index)
            if (dispose) disposable.dispose()
        }
    }

    /**
     * Disposes all held disposables.
     */
    fun disposeAll() {
        disposables.forEach { it.dispose() }
        disposables.clear()
    }

    override fun dispose() {
        disposeAll()
        super.dispose()
    }
}
