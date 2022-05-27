package pl.wojtach.silvarerum2

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import pl.wojtach.silvarerum2.utils.newChildScope

class ThrottledConsumer<T>(
    scope: CoroutineScope,
    timeoutMs: Long,
    consume: suspend (T) -> Unit
) : CoroutineScope by scope.newChildScope() {

    private val channel = Channel<T>(capacity = 1, onBufferOverflow = BufferOverflow.DROP_LATEST).apply {
        launch {
            consumeEach { element ->
                consume(element)
                delay(timeoutMs)
                tryReceive() // empty possible value received during timeout
            }
        }
    }

    fun send(element: T) {
        channel.trySend(element)
    }
}