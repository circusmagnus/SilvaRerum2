package pl.wojtach.silvarerum2

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pl.wojtach.silvarerum2.widgets.EditNoteButton

@Composable
fun ReadOnlyNoteScreen(noteId: NoteId, notes: Notes) {
    val note by notes.get(noteId).collectAsState(initial = null)
    val scope = rememberCoroutineScope()
    val editClicks = remember(key1 = notes, key2 = scope) {
        ThrottledConsumer<NoteSnapshot>(scope, 500) { notes.edit(it) }
    }
    ReadOnlyNote(
        content = note?.content ?: "",
        EditButton = { note?.let { noteSnapshot -> EditNoteButton { editClicks.send(noteSnapshot) } } }
    )
}

@Composable
fun ReadOnlyNote(content: String, EditButton: @Composable () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar {
                Row(modifier = Modifier.padding(8.dp)) {
                    Spacer(modifier = Modifier.weight(1f, fill = true))
                    EditButton()
                }

            }
        }
    ) {
        Text(text = content)
    }
}