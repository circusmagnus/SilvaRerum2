package pl.wojtach.silvarerum2

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@Composable
fun EditNoteScreen(notes: Notes, noteId: NoteId) {

    Log.d("lw", "EditNoteScreen composed")

    val note: NoteSnapshot? by notes.get(noteId).collectAsState(initial = null)
    EditNote(content = note?.content ?: "", onValueChange = { newContent -> note?.let { notes.update(it, newContent) } })
}

@Composable
fun EditNote(content: String, onValueChange: (newContent: String) -> Unit) {
    Log.d("lw", "EditNote composed")

    TextField(value = content, onValueChange = onValueChange, label = { Text("Treść") })
}