package pl.wojtach.silvarerum2.statemachine

interface StateAccess<out STATE> {
    val currentState: STATE
}

interface SuspendingActionScope<STATE, in EVENT> : StateAccess<STATE> {

    suspend fun changeState(block: (STATE) -> STATE)

    suspend fun emitEvent(emission: (STATE) -> EVENT?)
}

interface ActionScope<STATE, in EVENT> : StateAccess<STATE> {

    fun changeState(block: (STATE) -> STATE)

    fun emitEvent(emission: (STATE) -> EVENT?)
}