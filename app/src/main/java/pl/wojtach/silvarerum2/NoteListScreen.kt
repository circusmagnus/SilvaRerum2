package pl.wojtach.silvarerum2

import android.util.Log
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
import pl.wojtach.silvarerum2.widgets.AddNoteButton
import pl.wojtach.silvarerum2.widgets.DeleteNoteButton
import pl.wojtach.silvarerum2.widgets.EditNoteButton
import pl.wojtach.silvarerum2.widgets.NoteList
import pl.wojtach.silvarerum2.widgets.ShortNote

@Composable
fun NoteListScreen(onNoteClick: (NoteSnapshot) -> Unit, onNoteAdd: (NoteSnapshot) -> Unit, onNoteEdit: (NoteSnapshot) -> Unit) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val model = remember(key1 = scope) { notesDeps().noteListModel(scope) }

    val addNoteClicks = remember(key1 = scope, key2 = model) {
        ThrottledConsumer<Unit>(scope, 500) {
            val newNote = model.addNew()
            onNoteAdd(newNote)
        }
    }

    val editNoteClicks = remember(key1 = scope, key2 = model) {
        ThrottledConsumer<NoteSnapshot>(scope, 500) { note -> onNoteEdit(note) }
    }

    val currentList by model.state.collectWhileStarted(lifecycleOwner = lifecycleOwner)

    Log.d("lw", "NoteListScreen composed")

    NoteListLayout(
        topBar = { TopAppBar { Text(modifier = Modifier.padding(8.dp), text = "Silva Rerum") } },
        noteListUi = {
            NoteList(noteList = currentList, ShortNoteCellFact = { note ->
                ShortNote(
                    note = note,
                    onClick = { onNoteClick(note) },
                    DeleteButton = { DeleteNoteButton { model.delete(note) } },
                    EditButton = { EditNoteButton { editNoteClicks.send(note) } })
            })
        },
        floatingButton = { AddNoteButton { addNoteClicks.send(Unit) } }
    )
}

@Composable
fun NoteListLayout(topBar: @Composable () -> Unit, noteListUi: @Composable () -> Unit, floatingButton: @Composable () -> Unit) {

    Log.d("lw", "NoteListLayout composed")

    Scaffold(
        topBar = topBar,
        floatingActionButton = floatingButton,
        content = { noteListUi() }
    )
}

