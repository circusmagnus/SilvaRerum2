package pl.wojtach.silvarerum2.utils

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

fun <T, R> Flow<T>.mapLatest(transform: suspend (value: T) -> R): Flow<R> = flow {
    coroutineScope {
        val channel = Channel<R>(Channel.BUFFERED)
        launch {
            var ongoingTransformation: Job? = null
            collect { value ->
                ongoingTransformation?.cancelAndJoin()
                ongoingTransformation = launch(start = CoroutineStart.UNDISPATCHED) {
                    channel.send(transform(value))
                }
            }
            ongoingTransformation?.join()
            channel.close()
        }
        channel.consumeEach { emit(it) }
    }
}