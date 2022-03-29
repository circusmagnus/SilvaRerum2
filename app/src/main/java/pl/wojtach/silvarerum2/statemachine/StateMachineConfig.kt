package pl.wojtach.silvarerum2.statemachine

data class StateMachineConfig(
    val stopDelayMillis: Long = 5_000,
    val capacity: Int = 1024
)