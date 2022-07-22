package pl.wojtach.silvarerum2

import android.util.Log
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pl.wojtach.silvarerum2.manualdi.notesDeps
import pl.wojtach.silvarerum2.widgets.SilvaRerumHeader
import pl.wojtach.silvarerum2.widgets.UndoButton

@Composable
fun EditNoteScreen(noteSnapshot: NoteSnapshot) {

    Log.d("lw", "EditNoteScreen composed")
    val scope = rememberCoroutineScope()
    val model = remember(key1 = scope) { notesDeps().editNoteModel(scope, noteSnapshot) }

    val viewState by model.state

    EditNoteLayout(
        EditNoteField = { TextField(value = viewState.note.content, onValueChange = model::edit, label = { Text("Treść") }) },
        UndoButton = { if (viewState.undoEnabled) UndoButton(model::undo) else Unit }
    )
}

@Composable
fun EditNoteLayout(UndoButton: @Composable () -> Unit, EditNoteField: @Composable () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar {
                Row(modifier = Modifier.padding(8.dp)) {
                    SilvaRerumHeader(modifier = Modifier.weight(1f, fill = true))
                    UndoButton()
                }
            }
        },
        content = { EditNoteField() }
    )
}