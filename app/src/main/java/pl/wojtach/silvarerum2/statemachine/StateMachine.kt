package pl.wojtach.silvarerum2.statemachine

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart.DEFAULT
import kotlinx.coroutines.CoroutineStart.LAZY
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingCommand
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pl.allegro.android.buyers.common.util.events.OneTimeEvent
import pl.allegro.android.buyers.common.util.events.SharedEvents
import pl.allegro.android.buyers.common.util.events.filterNotHandledBy
import pl.wojtach.silvarerum2.utils.cancelChildren
import pl.wojtach.silvarerum2.utils.mapLatest
import pl.wojtach.silvarerum2.utils.newSupervisingChildScope

private typealias Action<STATE, EVENT> = suspend IStateMachine<STATE, EVENT>.() -> Unit
private typealias StateChange<STATE> = (STATE) -> STATE
private typealias EventEmission<STATE, EVENT> = (STATE) -> EVENT?

/**
 * A state container, redux / MVI style. It contains state and can emit one-time events. To interact, use actions and processes.
 *
 * When it has subscribers (anybody listening to state changes or events), it is in Started state
 * When all subscribers disappear it waits for specified delay and (if no new subscribers appear) stops.
 * Stopping means cancelling all ongoing processes (but not actions).
 *
 * When State Machine starts again it restarts previously cancelled processes. Completed or failed processes are not going to
 * be automatically restarted.
 *
 * Cancelling State Machine`s coroutine scope, cancels State Machine and all its actions / processes definitely.
 */
class StateMachine<STATE, EVENT>(
    private val scope: CoroutineScope,
    private val initialState: STATE,
    private val config: StateMachineConfig = StateMachineConfig()
) : IStateMachine<STATE, EVENT> {

    private val taskQueue = Channel<Task<STATE, EVENT>>(config.capacity)
    private val actionsScope = scope.newSupervisingChildScope()
    private val stateAndEvents = taskQueue
        .consumeAsFlow()
        .process()
        .shareIn(scope, SubscriptionAwareStrat(config.stopDelayMillis), config.capacity)

    override val state: StateFlow<STATE> = stateAndEvents
        .filterIsInstance<StateOrEvent.State<STATE>>()
        .map { it.value }
        .stateIn(scope, started = SharingStarted.WhileSubscribed(), initialState)

    override val events: SharedEvents<EVENT> = object : SharedEvents<EVENT> {

        private val cachedEvents = stateAndEvents
            .filterIsInstance<StateOrEvent.Event<EVENT>>()
            .map { it.value }
            .shareIn(scope, SharingStarted.WhileSubscribed(), config.capacity)

        /**
         * An event will be delivered only once to a unique consumerId. For example pass here an activity / fragment TAG string,
         * so that a new instance of the same fragment will not receive same event again after rotation or other config change.
         */
        override fun getFor(consumerId: String): Flow<EVENT> = cachedEvents.filterNotHandledBy(consumerId)
    }

    override fun action(block: Action<STATE, EVENT>) {
        taskQueue.trySend(Task.Action(block))
    }

    override fun changeState(block: (STATE) -> STATE) {
        taskQueue.trySendOrThrow(Task.RunMutation(block))
    }

    override fun emitEvent(emission: (STATE) -> EVENT?) {
        taskQueue.trySendOrThrow(Task.EmitEvent((emission)))
    }

    private fun <T> SendChannel<T>.trySendOrThrow(value: T) {
        val didSend = trySend(value).isSuccess
        if (!didSend) {
            throw IllegalStateException(
                "Task queue of this ${this@StateMachine} has exceeded its max capacity of ${config.capacity} elements"
            )
        }
    }

    private fun Flow<Task<STATE, EVENT>>.process(): Flow<StateOrEvent<STATE, EVENT>> = flow {
        val ongoingJobs = mutableMapOf<Job, Action<STATE, EVENT>>()
        var isStarted = false
        var state = initialState

        collect { task ->
            when (task) {
                Task.Start          -> {
                    isStarted = true
                    startMachine(ongoingJobs)
                }
                Task.Stop           -> {
                    isStarted = false
                    actionsScope.cancelChildren()
                }
                is Task.EmitEvent   -> task.eventEmission(state)?.let { emit(StateOrEvent.Event(OneTimeEvent(it))) }
                is Task.RunMutation -> {
                    state = task.stateChange(state)
                    emit(StateOrEvent.State(state))
                }
                is Task.Action      -> {
                    val newJob = launchAction(isStarted, task.block)
                    ongoingJobs[newJob] = task.block
                }
                is Task.FlushJob    -> ongoingJobs.remove(task.job)
            }
        }
    }

    private fun startMachine(ongoingJobs: MutableMap<Job, Action<STATE, EVENT>>){
        ongoingJobs.forEach { (job, action) ->
            if (job.isCancelled) {
                val newJob = launchAction(true, action)
                ongoingJobs.remove(job)
                ongoingJobs[newJob] = action
            } else {
                job.start()
            }
        }
    }

    private fun launchAction(isMachineStarted: Boolean, action: Action<STATE, EVENT>): Job {
        val newJob = actionsScope.launch(start = if (isMachineStarted) DEFAULT else LAZY) {
            try {
                action()
            } finally {
                taskQueue.send(Task.FlushJob(this.coroutineContext[Job]!!))
            }
        }
        return newJob
    }

    private inner class SubscriptionAwareStrat(private val stopDelayMs: Long) : SharingStarted {

        override fun command(subscriptionCount: StateFlow<Int>): Flow<SharingCommand> = subscriptionCount
            .startStopAsyncTasks()
            .keepSharing()

        private fun Flow<Int>.startStopAsyncTasks() = mapLatest { count -> toStateMachineTask(count) }
            .dropWhile { it != Task.Start } // do not stop before we start
            .distinctUntilChanged() //  avoid repeated "starts"
            .onEach { localCommand -> taskQueue.send(localCommand) }

        private suspend fun toStateMachineTask(subscriptionCount: Int) = if (subscriptionCount > 0) {
            Task.Start
        } else {
            delay(stopDelayMs)
            Task.Stop
        }

        private fun Flow<Task<STATE, EVENT>>.keepSharing() = map { SharingCommand.START }.distinctUntilChanged()
    }
}

private sealed class Task<out STATE, out EVENT> {
    data class Action<STATE, EVENT>(val block: suspend IStateMachine<STATE, EVENT>.() -> Unit) : Task<STATE, EVENT>()
    object Start : Task<Nothing, Nothing>()
    object Stop : Task<Nothing, Nothing>()
    data class RunMutation<STATE>(val stateChange: StateChange<STATE>) : Task<STATE, Nothing>()
    data class EmitEvent<STATE, EVENT>(val eventEmission: EventEmission<STATE, EVENT>) : Task<STATE, EVENT>()
    data class FlushJob(val job: Job): Task<Nothing, Nothing>()
}

private sealed class StateOrEvent<out STATE, out EVENT> {
    data class State<out STATE>(val value: STATE) : StateOrEvent<STATE, Nothing>()
    data class Event<out EVENT>(val value: OneTimeEvent<EVENT>) : StateOrEvent<Nothing, EVENT>()
}