package pl.wojtach.silvarerum2

import android.util.Log
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalLifecycleOwner
import pl.wojtach.silvarerum2.manualdi.notesDeps
import pl.wojtach.silvarerum2.utils.collectWhileStarted

@Composable
fun EditNoteScreen(noteSnapshot: NoteSnapshot) {

    Log.d("lw", "EditNoteScreen composed")
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val model = remember(key1 = scope) { notesDeps().editNoteModel(scope, noteSnapshot) }

    val currentContent by model.state.collectWhileStarted(lifecycleOwner = lifecycleOwner)

    EditNote(content = currentContent.content, onValueChange = { newContent -> model.edit(newContent)} )
}

@Composable
fun EditNote(content: String, onValueChange: (newContent: String) -> Unit) {
    Log.d("lw", "EditNote composed")

    TextField(value = content, onValueChange = onValueChange, label = { Text("Treść") })
}