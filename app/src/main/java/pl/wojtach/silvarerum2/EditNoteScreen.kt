package pl.wojtach.silvarerum2

import android.util.Log
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalLifecycleOwner
import pl.wojtach.silvarerum2.utils.collectWhileStarted

@Composable
fun EditNoteScreen(notes: Notes, noteId: NoteId) {

    Log.d("lw", "EditNoteScreen composed")

    val lifecycleOwner = LocalLifecycleOwner.current
    val note: NoteSnapshot? by notes.get(noteId).collectWhileStarted(lifecycleOwner, initialValue = null)
    EditNote(content = note?.content ?: "", onValueChange = { newContent -> note?.let { notes.update(it, newContent) } })
}

@Composable
fun EditNote(content: String, onValueChange: (newContent: String) -> Unit) {
    Log.d("lw", "EditNote composed")

    TextField(value = content, onValueChange = onValueChange, label = { Text("Treść") })
}