package com.eka.medassist.ui.chat.common

/**
 * Messages waiting to go out, oldest first: typed while offline, or a message lost when the
 * connection dropped (re-sent once automatically). Pure logic, so it's unit-tested.
 */
class Outbox<T> {
    private val items = ArrayDeque<Pair<String, T>>()
    private val autoRetried = mutableSetOf<String>()

    val size: Int get() = items.size
    fun isEmpty() = items.isEmpty()
    fun ids(): List<String> = items.map { it.first }

    fun add(id: String, item: T) {
        if (items.none { it.first == id }) items.addLast(id to item)
    }

    /**
     * Queue a lost message for one automatic resend. Returns false if it was already
     * auto-resent once (then the doctor decides, with Retry).
     */
    fun addRetry(id: String, item: T): Boolean {
        if (!autoRetried.add(id)) return false
        if (items.none { it.first == id }) items.addFirst(id to item)
        return true
    }

    fun next(): Pair<String, T>? = items.removeFirstOrNull()

    fun remove(id: String) = items.removeAll { it.first == id }

    fun clear() {
        items.clear()
        autoRetried.clear()
    }
}
