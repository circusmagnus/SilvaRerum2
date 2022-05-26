package pl.wojtach.silvarerum2

import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import pl.wojtach.silvarerum2.utils.collectWhileStarted
import pl.wojtach.silvarerum2.widgets.AddNoteButton
import pl.wojtach.silvarerum2.widgets.DeleteNoteButton
import pl.wojtach.silvarerum2.widgets.EditNoteButton
import pl.wojtach.silvarerum2.widgets.NoteList
import pl.wojtach.silvarerum2.widgets.ShortNote

@Composable
fun NoteListScreen(notes: Notes) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val noteListData by notes.all.collectWhileStarted(lifecycleOwner = lifecycleOwner)

    Log.d("lw", "NoteListScreen composed")

    NoteListLayout(
        topBar = { TopAppBar { Text(modifier = Modifier.padding(8.dp), text = "Silva Rerum") } },
        noteListUi = {
            NoteList(noteList = { noteListData }, ShortNoteCellFact = { note ->
                ShortNote(
                    note = { note },
                    onClick = { notes.noteClicked(note.noteId) },
                    DeleteButton = { DeleteNoteButton { notes.delete(note) } },
                    EditButton = { EditNoteButton { notes.edit(note) } })
            })
        },
        floatingButton = { AddNoteButton { notes.add("") } }
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

