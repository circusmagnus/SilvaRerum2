package pl.wojtach.silvarerum2

import android.util.Log
import androidx.compose.material.Scaffold
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalLifecycleOwner
import pl.wojtach.silvarerum2.manualdi.notesDeps
import pl.wojtach.silvarerum2.utils.collectWhileStarted
import pl.wojtach.silvarerum2.widgets.AddNoteButton
import pl.wojtach.silvarerum2.widgets.DeleteNoteButton
import pl.wojtach.silvarerum2.widgets.EditNoteButton
import pl.wojtach.silvarerum2.widgets.NoteList
import pl.wojtach.silvarerum2.widgets.ShortNote
import pl.wojtach.silvarerum2.widgets.SilvaRerumHeader

@Composable
fun NoteListScreen(onNoteClick: (NoteSnapshot) -> Unit, onNoteAdd: (NoteSnapshot) -> Unit, onNoteEdit: (NoteSnapshot) -> Unit) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val model = remember(key1 = scope) { notesDeps().noteListModel(scope) }
    val currentList by model.state.collectWhileStarted(lifecycleOwner = lifecycleOwner)

    Log.d("lw", "NoteListScreen composed")

    NoteListLayout(
        topBar = { TopAppBar { SilvaRerumHeader() } },
        noteListUi = {
            NoteList(noteList = currentList, ShortNoteCellFact = { note ->
                ShortNote(
                    note = note,
                    onClick = { onNoteClick(note) },
                    DeleteButton = { DeleteNoteButton { model.delete(note) } },
                    EditButton = { EditNoteButton { onNoteEdit(note) } })
            })
        },
        floatingButton = {
            AddNoteButton {
                val newNote = model.addNew()
                onNoteAdd(newNote)
            }
        }
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

