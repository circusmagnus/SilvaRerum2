package pl.wojtach.silvarerum2

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import pl.wojtach.silvarerum2.manualdi.notesDeps
import pl.wojtach.silvarerum2.utils.collectWhileStarted
import pl.wojtach.silvarerum2.widgets.DeleteNoteButton
import pl.wojtach.silvarerum2.widgets.EditNoteButton
import pl.wojtach.silvarerum2.widgets.SilvaRerumHeader

@Composable
fun ReadOnlyNoteScreen(note: NoteSnapshot, toEditing: (NoteSnapshot) -> Unit, onDeletion: () -> Unit) {
    val scope = rememberCoroutineScope()
    val model = remember(key1 = scope) { notesDeps().readNoteModel(scope, note) }
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentNote by model.state.collectWhileStarted(lifecycleOwner)

    ReadOnlyNote(
        content = currentNote.content,
        EditButton = { EditNoteButton { toEditing(currentNote) } },
        DeleteButton = { DeleteNoteButton { model.delete(); onDeletion() } }
    )
}

@Composable
fun ReadOnlyNote(content: String, EditButton: @Composable () -> Unit, DeleteButton: @Composable () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar {
                Row(modifier = Modifier.padding(8.dp)) {
                    SilvaRerumHeader(modifier = Modifier.weight(1f, fill = true))
                    DeleteButton()
                    EditButton()
                }

            }
        }
    ) {
        Text(text = content)
    }
}