package pl.wojtach.silvarerum2.utils

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun <T> StateFlow<T>.collectWhileStarted(lifecycleOwner: LifecycleOwner): State<T> = produceState(
    initialValue = value,
    key1 = lifecycleOwner,
    producer = {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            collect { value = it }
        }
    }
)

@Composable
fun <T> Flow<T>.collectWhileStarted(lifecycleOwner: LifecycleOwner, initialValue: T): State<T> = produceState(
    initialValue = initialValue,
    key1 = lifecycleOwner,
    key2 = initialValue,
    producer = {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            collect { value = it }
        }
    }
)