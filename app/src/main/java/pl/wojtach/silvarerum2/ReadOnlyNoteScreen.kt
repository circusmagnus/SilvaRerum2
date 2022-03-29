package pl.wojtach.silvarerum2

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import pl.wojtach.silvarerum2.ui.theme.Typography
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@Composable
fun ReadOnlyNoteScreen(noteId: NoteId, notes: Notes) {
    val note by notes.get(noteId).collectAsState(initial = null)
    val title = note?.content?.takeWhile { char -> char != ' ' } ?: ""
    ReadOnlyNote(title = title, content = note?.content ?: "")
}

@Composable
fun ReadOnlyNote(title: String, content: String){
    Column {
        Text(style = Typography.h5, text = title)
        Text(text = content)
    }
}