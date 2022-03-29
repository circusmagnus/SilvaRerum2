package pl.wojtach.silvarerum2.statemachine

import kotlinx.coroutines.flow.StateFlow
import pl.allegro.android.buyers.common.util.events.SharedEvents

interface IStateMachine<STATE, EVENT> {
    val state: StateFlow<STATE>
    val events: SharedEvents<EVENT>

    fun action(block: suspend IStateMachine<STATE, EVENT>.() -> Unit)

    fun changeState(block: (STATE) -> STATE)

    fun emitEvent(emission: (STATE) -> EVENT?)
}