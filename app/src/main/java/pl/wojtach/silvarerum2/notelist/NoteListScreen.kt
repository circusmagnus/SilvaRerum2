@file:OptIn(ExperimentalMaterial3Api::class)

package pl.wojtach.silvarerum2.notelist

import android.util.Log
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import pl.wojtach.silvarerum2.NoteSnapshot
import pl.wojtach.silvarerum2.utils.collectWhileStarted
import pl.wojtach.silvarerum2.widgets.AddButton
import pl.wojtach.silvarerum2.widgets.DeleteButton
import pl.wojtach.silvarerum2.widgets.EditNoteButton
import pl.wojtach.silvarerum2.widgets.ExpandableSearch
import pl.wojtach.silvarerum2.widgets.ReorderableList
import pl.wojtach.silvarerum2.widgets.ShortNote
import pl.wojtach.silvarerum2.widgets.SilvaRerumHeader

@Composable
internal fun NoteListScreen(
    model: SearchableListModel,
    onNoteClick: (NoteSnapshot) -> Unit,
    onNoteAdd: (NoteSnapshot) -> Unit,
    onNoteEdit: (NoteSnapshot) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val state by model.searchedState.collectWhileStarted(lifecycleOwner = lifecycleOwner)

    val listCellFactory: @Composable (Modifier, NoteSnapshot) -> Unit = { modifier, note ->
        ShortNote(
            modifier = modifier,
            note = note,
            onClick = { onNoteClick(note) },
            DeleteButton = { DeleteButton { model.delete(note) } },
            EditButton = { EditNoteButton { onNoteEdit(note) } }
        )
    }

    Log.d("lw", "NoteListScreen composed")

    NoteListLayout(
        topBar = {
            TopAppBar(title = {
                NoteListHeader(
                    searchedPhrase = state.searchState.phrase,
                    onSearchedPhrase = { phrase -> model.searchFor(phrase) }
                )
            }
            )
        },
        noteListUi = { paddingValues ->
            ReorderableList(
                Modifier.padding(paddingValues),
                reorderableItems = state.notes,
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
fun NoteListLayout(topBar: @Composable () -> Unit, noteListUi: @Composable (PaddingValues) -> Unit, floatingButton: @Composable () -> Unit) {

    Log.d("lw", "NoteListLayout composed")

    Scaffold(
        topBar = topBar,
        floatingActionButton = floatingButton,
        content = { paddingValues ->  noteListUi(paddingValues) }
    )
}

@Composable
fun NoteListHeader(
    searchedPhrase: String,
    onSearchedPhrase: (String) -> Unit
) {
    var localSearch by remember { mutableStateOf(searchedPhrase) }
    var isSearchActive by remember { mutableStateOf(searchedPhrase.isNotEmpty()) }

    Row {
        if (!isSearchActive) {
            SilvaRerumHeader()
            Spacer(modifier = Modifier.weight(1f))
        }
        ExpandableSearch(
            isSearchActive,
            onToggle = { isActive ->
                if (!isActive) {
                    localSearch = ""
                    onSearchedPhrase("")
                }
                isSearchActive = isActive
            },
            searchPhrase = localSearch,
            onSearchedPhrase = { phrase ->
                localSearch = phrase
                onSearchedPhrase(phrase)
            }
        )
    }
}

