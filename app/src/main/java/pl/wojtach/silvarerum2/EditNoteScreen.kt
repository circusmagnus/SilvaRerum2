package pl.wojtach.silvarerum2

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@Composable
fun EditNoteScreen(notes: Notes, noteId: NoteId) {
    val note: NoteSnapshot? by notes.get(noteId).collectAsState(initial = null)
    EditNote(content = note?.content ?: "", onValueChange = { newContent -> note?.let { notes.update(it, newContent) } })
}

@Composable
fun EditNote(content: String, onValueChange: (newContent: String) -> Unit) {
        TextField(value = content, onValueChange = onValueChange, label = { Text("Treść") })
}