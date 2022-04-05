package pl.wojtach.silvarerum2

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun NoteListScreen(notes: Notes) {
    val noteListData by notes.all.collectAsState()
    NoteListLayout(
        topBar = { TopAppBar { Text(modifier = Modifier.padding(8.dp), text = "Silva Rerum") } },
        noteListUi = {
            NoteList(noteList = noteListData, ShortNoteCellFact = { note ->
                ShortNote(
                    note = note,
                    onClick = { notes.noteClicked(note.id) },
                    DeleteButton = { DeleteNoteButton { notes.delete(note)} },
                    EditButton = { EditNoteButton { notes.edit(note) } })
            })
        },
        floatingButton = { AddNoteButton { notes.add("") } }
    )
}

@Composable
fun NoteListLayout(topBar: @Composable () -> Unit, noteListUi: @Composable () -> Unit, floatingButton: @Composable () -> Unit) {
    Scaffold(
        topBar = topBar,
        floatingActionButton = floatingButton,
        content = { noteListUi() }
    )
}

@Composable
@Preview
fun AddNoteButton(onClick: () -> Unit = {}) {
    FloatingActionButton(onClick = onClick, modifier = Modifier) {
        Icon(Icons.Filled.Add, contentDescription = "Add note")
    }
}

@Composable
@Preview
fun DeleteNoteButton(onClick: () -> Unit = {}) {
    IconButton(onClick = onClick) {
        Icon(Icons.Filled.Delete, contentDescription = "delete")
    }
}

@Composable
@Preview
fun EditNoteButton(onClick: () -> Unit = {}) {
    IconButton(onClick = onClick) {
        Icon(Icons.Filled.Edit, contentDescription = "delete")
    }
}

@Composable
fun NoteList(noteList: List<NoteSnapshot>, ShortNoteCellFact: @Composable (NoteSnapshot) -> Unit) {
    LazyColumn {
        items(
            noteList,
            { note -> note.id.value },
            { note -> ShortNoteCellFact(note) }
        )
    }
}

@Composable
fun ShortNote(
    note: NoteSnapshot = NoteSnapshot(NoteId("a"), Timestamp(System.currentTimeMillis()), "Ala ma kota"),
    onClick: () -> Unit,
    DeleteButton: @Composable () -> Unit,
    EditButton: @Composable () -> Unit
//    onEditClicked: (note: NoteSnapshot) -> Unit,
//    onDeleteClicked: (note: NoteSnapshot) -> Unit
) {
    Card(border = BorderStroke(1.dp, color = Color.Black)) {
        Row(Modifier.clickable { onClick() }) {
            Text(text = note.content, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(8.dp).weight(4f, true))
            Row(modifier = Modifier.weight(1f, false)) {
                DeleteButton()
                EditButton()
            }
        }

    }
}