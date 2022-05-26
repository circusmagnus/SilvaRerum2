package pl.wojtach.silvarerum2.utils

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onEach

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun <T> StateFlow<T>.collectWhileStarted(lifecycleOwner: LifecycleOwner): State<T> = produceState(
    initialValue = value,
    key1 = lifecycleOwner,
    producer = {
        flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { Log.d("lw", "collecting state while started. State: $it") }
            .collect { value = it }
    }
)