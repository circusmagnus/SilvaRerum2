@file:OptIn(ExperimentalMaterial3Api::class)

package pl.wojtach.silvarerum2

import android.util.Log
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pl.wojtach.silvarerum2.manualdi.notesComponent
import pl.wojtach.silvarerum2.widgets.SilvaRerumHeader
import pl.wojtach.silvarerum2.widgets.UndoButton

@Composable
internal fun EditNoteScreen(noteSnapshot: NoteSnapshot) {

    Log.d("lw", "EditNoteScreen composed")
    val scope = rememberCoroutineScope()
    val model = remember(key1 = scope) { notesComponent().editNoteModel(scope, noteSnapshot) }

    val viewState by model.state

    EditNoteLayout(
        EditNoteField = { padding ->
            TextField(
                modifier = Modifier.padding(padding),
                value = viewState.note.content,
                onValueChange = model::edit,
                label = { Text("Treść") })
        },
        UndoButton = { if (viewState.undoEnabled) UndoButton(model::undo) else Unit }
    )
}

@Composable
internal fun EditNoteLayout(UndoButton: @Composable () -> Unit, EditNoteField: @Composable (PaddingValues) -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(title = {
                Row(modifier = Modifier.padding(8.dp)) {
                    SilvaRerumHeader(modifier = Modifier.weight(1f, fill = true))
                    UndoButton()
                }
            }
            )
        },
        content = { paddingValues -> EditNoteField(paddingValues) }
    )
}