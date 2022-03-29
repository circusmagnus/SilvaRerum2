package pl.wojtach.silvarerum2.statemachine

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.fold
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.runningFold
import kotlinx.coroutines.flow.stateIn

class MiniStateMachine<STATE, EVENT>(scope: CoroutineScope, initialState: STATE, capacity: Int = 128) {

    private val inbox = Channel<(STATE) -> STATE>(capacity = capacity)

    val state: StateFlow<STATE> = inbox.consumeAsFlow()
        .runningFold(initialState) { state, mutation -> mutation(state) }
        .stateIn(scope, started = SharingStarted.Eagerly, initialState)


    fun changeState(stateChange : (STATE) -> STATE) {
        val result = inbox.trySend(stateChange)
        check(result.isSuccess) { "Queue is full" }
    }
}