package pl.allegro.android.buyers.common.util.events

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.transform

fun <T> Flow<OneTimeEvent<T>>.filterNotHandledBy(consumerId: String): Flow<T> = transform { event ->
    event.getIfNotHandled(consumerId)?.let { emit(it) }
}

interface SharedEvents<out T> {

    fun getFor(consumerId: String): Flow<T>
}

class EventQueue<T>(capacity: Int) : SharedEvents<T> {

    private val innerQueue = MutableSharedFlow<OneTimeEvent<T>>(replay = capacity)

    suspend fun push(event: T) {
        innerQueue.emit(OneTimeEvent(event))
    }

    fun tryPush(event: T) = innerQueue.tryEmit(OneTimeEvent(event))

    override fun getFor(consumerId: String): Flow<T> = innerQueue.filterNotHandledBy(consumerId)
}