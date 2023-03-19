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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import pl.wojtach.silvarerum2.NoteSnapshot
import pl.wojtach.silvarerum2.notelist.SearchableListModel.Search
import pl.wojtach.silvarerum2.utils.collectWhileStarted
import pl.wojtach.silvarerum2.widgets.AddButton
import pl.wojtach.silvarerum2.widgets.DeleteButton
import pl.wojtach.silvarerum2.widgets.EditNoteButton
import pl.wojtach.silvarerum2.widgets.ExpandableSearch
import pl.wojtach.silvarerum2.widgets.ReorderableList
import pl.wojtach.silvarerum2.widgets.ShortNote
import pl.wojtach.silvarerum2.widgets.SilvaRerumHeader

@Composable
fun NoteListScreen(
    model: NoteListModel,
    search: Search,
    onNoteClick: (NoteSnapshot) -> Unit,
    onNoteAdd: (NoteSnapshot) -> Unit,
    onNoteEdit: (NoteSnapshot) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    var searchPhrase by rememberSaveable {
        mutableStateOf("")
    }
    var isSearchActive by rememberSaveable {
        mutableStateOf(false)
    }
    val searchEngine = rememberSearch(
        noteList = model.state,
        initialState = Search(isSearchActive, searchPhrase)
    )
    val notes by searchEngine.searched.collectWhileStarted(
        lifecycleOwner = lifecycleOwner,
        initialValue = emptyList()
    )

    val listCellFactory: @Composable (Modifier, NoteSnapshot) -> Unit = { modifier, note ->
        ShortNote(
            modifier = modifier,
            note = note,
            onClick = { onNoteClick(note) },
            DeleteButton = { DeleteButton { model.delete(note) } },
            EditButton = { EditNoteButton { onNoteEdit(note) } }
        )
    }

    Log.d("lw", "NoteListScreen composed. Search phrase: $searchPhrase, searchActive = $isSearchActive, engine: $searchEngine, notes: $notes")

    NoteListLayout(
        topBar = {
            TopAppBar(title = {
                NoteListHeader(
                    isSearchActive = isSearchActive,
                    SearchSection = {
                        ExpandableSearch(
                            isSearchActive = isSearchActive,
                            onToggle = { active ->
                                isSearchActive = active
                                if (active) searchEngine.setActive() else searchEngine.disable()
                            },
                            searchPhrase = searchPhrase,
                            onSearchedPhrase = { phrase ->
                                searchPhrase = phrase
                                searchEngine.searchFor(phrase)
                            }
                        )
                    }
                )
            }
            )
        },
        noteListUi = { paddingValues ->
            ReorderableList(
                Modifier.padding(paddingValues),
                reorderableItems = notes,
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
fun NoteListLayout(
    topBar: @Composable () -> Unit,
    noteListUi: @Composable (PaddingValues) -> Unit,
    floatingButton: @Composable () -> Unit
) {

    Log.d("lw", "NoteListLayout composed")

    Scaffold(
        topBar = topBar,
        floatingActionButton = floatingButton,
        content = { paddingValues -> noteListUi(paddingValues) }
    )
}

@Composable
fun NoteListHeader(
    isSearchActive: Boolean,
    SearchSection: @Composable () -> Unit
) {

    Row {
        if (!isSearchActive) {
            SilvaRerumHeader()
            Spacer(modifier = Modifier.weight(1f))
        }
        SearchSection()
    }
}

