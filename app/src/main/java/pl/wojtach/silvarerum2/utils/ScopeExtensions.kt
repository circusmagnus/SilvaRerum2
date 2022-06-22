package pl.wojtach.silvarerum2.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job

/**
 * Creates a child scope.
 * If parents is canceled, so is the child.
 * If child is canceled, parent stays active.
 * If child is failed due to some throwable, all coroutines in this child scope get canceled and exception is propagated to
 * parent. Any CoroutineExceptionHandler installed in child has no effect.
 */
fun CoroutineScope.newChildScope(): CoroutineScope = CoroutineScope(coroutineContext + Job(parent = coroutineContext[Job]))