package pl.wojtach.silvarerum2

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun NoteListScreen(notes: Notes onClick:) {
    val noteList by notes.all.collectAsState()
}

@Composable
fun NoteList(noteList: List<NoteSnapshot>) {
    LazyColumn {
        items(
            noteList,
            { note -> note.id },
            { note -> ShortNote(note = note) }
        )
    }
}

@Composable
@Preview
fun ShortNote(note: NoteSnapshot = NoteSnapshot(NoteId("a"), Timestamp(System.currentTimeMillis()), "Ala ma kota")) {
    Text(text = note.content, maxLines = 2, overflow = TextOverflow.Ellipsis)
}