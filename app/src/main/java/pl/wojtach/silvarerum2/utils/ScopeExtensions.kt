package pl.wojtach.silvarerum2.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren

/**
 * Creates a child scope.
 * If parents is canceled, so is the child.
 * If child is canceled, parent stays active.
 * If child is failed due to some throwable, all coroutines in this child scope get canceled and exception is propagated to
 * parent. Any CoroutineExceptionHandler installed in child has no effect.
 */
fun CoroutineScope.newChildScope(): CoroutineScope = CoroutineScope(coroutineContext + Job(parent = coroutineContext[Job]))

/**
 * Creates a child scope.
 * If parents is canceled, so is the child.
 * If child is canceled, parent stays active.
 * If child is failed due to some throwable, other coroutines in this child scope continue working and failure is not propagated
 * to parent scope. Failures may be handled by CoroutineException Handler installed within:
 *
 * val parentScope = CoroutineScope(Dispatchers.Default)
 * val supervisingChildWithHandler = parentScope.newSupervisingChildScope() + CoroutineExceptionHandler { _, throwable ->  }
 */
fun CoroutineScope.newSupervisingChildScope(): CoroutineScope =
    CoroutineScope(coroutineContext + SupervisorJob(parent = coroutineContext[Job]))

/**
 * Cancels all coroutines launched within this scope, without cancelling the scope itself - so that new coroutines may still be
 * launched within this scope
 */
fun CoroutineScope.cancelChildren() = coroutineContext.cancelChildren()