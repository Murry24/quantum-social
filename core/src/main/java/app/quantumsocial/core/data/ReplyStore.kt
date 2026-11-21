package app.quantumsocial.core.data

import app.quantumsocial.core.model.Reply
import app.quantumsocial.core.model.ReplyThread
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Odpovede na signály (bod 7) – zatiaľ len v pamäti. Vlákno zmizne spolu so signálom. */
object ReplyStore {
    private val _threads = MutableStateFlow<Map<String, ReplyThread>>(emptyMap())
    val threads: StateFlow<Map<String, ReplyThread>> = _threads.asStateFlow()

    /** @return false, ak už vlákno má max počet odpovedí */
    fun reply(
        signalId: String,
        reply: Reply,
    ): Boolean {
        val thread = _threads.value[signalId] ?: ReplyThread(signalId)
        if (!thread.canReply) return false
        _threads.value = _threads.value + (signalId to thread.add(reply))
        return true
    }

    /** Volá [InMemorySignalRepository], keď signály dozhasnú. */
    fun drop(signalIds: Set<String>) {
        if (signalIds.any { it in _threads.value }) _threads.value = _threads.value - signalIds
    }

    fun clear() {
        _threads.value = emptyMap()
    }
}
