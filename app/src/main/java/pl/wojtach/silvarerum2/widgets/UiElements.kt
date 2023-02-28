@file:OptIn(ExperimentalMaterial3Api::class)

package pl.wojtach.silvarerum2.widgets

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pl.wojtach.silvarerum2.NoteId
import pl.wojtach.silvarerum2.NoteSnapshot
import pl.wojtach.silvarerum2.Timestamp

@Composable
@Preview
fun AddButton(onClick: () -> Unit = {}) {
    FloatingActionButton(onClick = onClick) {
        Icon(Icons.Filled.Add, contentDescription = "Add note")
    }
}

@Composable
@Preview
fun DeleteButton(modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    var showingDialog by remember { mutableStateOf(false) }

    IconButton(
        onClick = {
            showingDialog = true
        },
        content = {
            Icon(Icons.Filled.Delete, contentDescription = "delete")
        }
    )

    if (showingDialog) {
        AlertDialog(
            title = { Text(text = "Delete?") },
            onDismissRequest = { showingDialog = false },
            dismissButton = { UndoButton { showingDialog = false } },
            confirmButton = { OkButton { showingDialog = false; onClick() } }
        )
    }
}

@Composable
@Preview
fun EditNoteButton(modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    IconButton(onClick = onClick) {
        Icon(Icons.Filled.Edit, contentDescription = "delete")
    }
}

@Composable
@Preview
fun UndoButton(onClick: () -> Unit = {}) {
    IconButton(onClick = onClick) {
        Icon(Icons.Filled.Undo, contentDescription = "undo")
    }
}

@Composable
@Preview
fun OkButton(onClick: () -> Unit = {}) {
    IconButton(onClick = onClick) {
        Icon(Icons.Filled.Check, contentDescription = "check")
    }
}

@Composable
fun ShortNote(
    modifier: Modifier = Modifier,
    note: NoteSnapshot = NoteSnapshot(
        NoteId("a"),
        created = Timestamp(System.currentTimeMillis()),
        "Ala ma kota",
        reversedShowIndex = 0
    ),
    onClick: () -> Unit,
    DeleteButton: @Composable () -> Unit,
    EditButton: @Composable () -> Unit
) {
    Card(modifier = modifier, border = BorderStroke(1.dp, color = Color.Black)) {
        Row(Modifier.clickable { onClick() }) {
            Text(
                text = note.content, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier
                    .padding(8.dp)
                    .weight(4f, true)
            )
            Row(modifier = Modifier.weight(1f, false)) {
                DeleteButton()
                EditButton()
            }
        }

    }
}

@Composable
@Preview
fun SilvaRerumHeader(modifier: Modifier = Modifier) {
    Text(modifier = modifier.padding(8.dp), text = "Silva Rerum")
}

@Composable
@Preview
fun ExpandableSearch(
    isSearchActive: Boolean = true,
    onToggle: (Boolean) -> Unit = {},
    searchPhrase: String = "kot",
    onSearchedPhrase: (String) -> Unit = {}
) {
    if (isSearchActive.not()) IconButton(onClick = { onToggle(true) }) {
        Icon(Icons.Filled.Search, contentDescription = "search")
    } else CloseableTextField(
        text = searchPhrase
    ) { phraseOrClose ->
        if (phraseOrClose != null) {
            onSearchedPhrase(phraseOrClose)
        } else {
            onToggle(false)
        }
    }
}

@Composable
@Preview
fun CloseableTextField(modifier: Modifier = Modifier, text: String = "", onEditOrClose: (String?) -> Unit = {}) {
    val focusRequester: FocusRequester = remember { FocusRequester() }
    Row(modifier) {
        TextField(
            modifier = Modifier.focusRequester(focusRequester),
            value = text,
            onValueChange = { newEdit -> onEditOrClose(newEdit) }
        )
        IconButton(onClick = { onEditOrClose(null) }) {
            Icon(Icons.Filled.Cancel, contentDescription = "Cancel search")
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}