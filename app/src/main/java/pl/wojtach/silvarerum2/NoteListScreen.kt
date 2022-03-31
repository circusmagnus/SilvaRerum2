package pl.wojtach.silvarerum2

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun NoteListScreen(notes: Notes) {
    val noteList by notes.all.collectAsState()
    Box(modifier = Modifier
        .fillMaxWidth()
        .fillMaxHeight()
        .padding(8.dp))
    NoteList(noteList = noteList, onClick = { note -> notes.noteClicked(note.id) })
    AddNoteButton {
        notes.add("")
    }
}

@Composable
fun AddNoteButton(onClick: () -> Unit) {
    FloatingActionButton(onClick = onClick) {
        Icon(Icons.Filled.Add, contentDescription = "Add note")
    }
}

@Composable
fun NoteList(noteList: List<NoteSnapshot>, onClick: (note: NoteSnapshot) -> Unit) {
    LazyColumn {
        items(
            noteList,
            { note -> note.id.value },
            { note -> ShortNote(note = note, onClick) }
        )
    }
}

@Composable
fun ShortNote(
    note: NoteSnapshot = NoteSnapshot(NoteId("a"), Timestamp(System.currentTimeMillis()), "Ala ma kota"),
    onClick: (note: NoteSnapshot) -> Unit,
//    onEditClicked: (note: NoteSnapshot) -> Unit,
//    onDeleteClicked: (note: NoteSnapshot) -> Unit
) {
    Text(text = note.content, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.clickable { onClick(note) })
}