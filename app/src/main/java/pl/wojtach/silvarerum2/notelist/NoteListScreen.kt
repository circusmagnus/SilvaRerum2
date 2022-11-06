package pl.wojtach.silvarerum2.notelist

import android.util.Log
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material.Scaffold
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import pl.wojtach.silvarerum2.NoteSnapshot
import pl.wojtach.silvarerum2.ui.collectWhileStarted
import pl.wojtach.silvarerum2.widgets.AddButton
import pl.wojtach.silvarerum2.widgets.DeleteNoteButton
import pl.wojtach.silvarerum2.widgets.EditNoteButton
import pl.wojtach.silvarerum2.widgets.ExpandableSearch
import pl.wojtach.silvarerum2.widgets.ReorderableList
import pl.wojtach.silvarerum2.widgets.ShortNote
import pl.wojtach.silvarerum2.widgets.SilvaRerumHeader

@Composable
fun NoteListScreen(model: SearchableListModel, onNoteClick: (NoteSnapshot) -> Unit, onNoteAdd: (NoteSnapshot) -> Unit, onNoteEdit: (NoteSnapshot) -> Unit) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentList by model.state.collectWhileStarted(lifecycleOwner = lifecycleOwner)

    val listCellFactory: @Composable (Modifier, NoteSnapshot) -> Unit = { modifier, note ->
        ShortNote(
            modifier = modifier,
            note = note,
            onClick = { onNoteClick(note) },
            DeleteButton = { DeleteNoteButton { model.delete(note) } },
            EditButton = { EditNoteButton { onNoteEdit(note) } }
        )
    }

    Log.d("lw", "NoteListScreen composed")

    NoteListLayout(
        topBar = { TopAppBar { NoteListHeader(model) } },
        noteListUi = {
            ReorderableList(
                reorderableItems = currentList,
                ListCell = listCellFactory,
                onReorder = { fromIndex, toIndex -> model.reorder(fromIndex, toIndex) }
            )
        },
        floatingButton = {
            AddButton {
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

@Composable
fun NoteListHeader(searchableListModel: SearchableListModel) {
    var isSearchActive by remember { mutableStateOf(false) }
    var searchedPhrase by remember { mutableStateOf("") }

    Row {
        if (!isSearchActive) {
            SilvaRerumHeader()
            Spacer(modifier = Modifier.weight(1f))
        }
        ExpandableSearch(
            isSearchActive,
            onToggle = { isActive ->
                if (!isActive) {
                    searchableListModel.searchFor(null)
                    searchedPhrase = ""
                }
                isSearchActive = isActive
            },
            searchPhrase = searchedPhrase,
            onSearchedPhrase = { phrase ->
                searchedPhrase = phrase
                searchableListModel.searchFor(phrase)
            }
        )
    }
}

