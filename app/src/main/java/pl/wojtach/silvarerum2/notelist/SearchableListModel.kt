package pl.wojtach.silvarerum2.notelist

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.saveable.rememberSaveable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import pl.wojtach.silvarerum2.NoteSnapshot
import pl.wojtach.silvarerum2.notelist.SearchableListModel.Search

@Composable
fun rememberSearch(noteList: Flow<List<NoteSnapshot>>, initialState: Search) = remember(
    key1 = noteList
) {
    SearchableListModel(noteList, initialState)
}

class SearchableListModel(
    noteList: Flow<List<NoteSnapshot>>,
    initialState: Search = Search()
) {

    private val searchState = MutableStateFlow(initialState)

    val searched: Flow<List<NoteSnapshot>> = noteList.combine(searchState) { allNotes, search ->
        val (isActive, phrase) = search

        if (shouldFilter(isActive, phrase)) applySearch(allNotes, phrase) else allNotes
    }

    private suspend fun applySearch(notes: List<NoteSnapshot>, phrase: String) =
        withContext(Dispatchers.Default) {
            notes.filter { note -> note.content.contains(phrase, ignoreCase = true) }
        }

    private fun shouldFilter(isActive: Boolean, phrase: String) = isActive && phrase.length > 2

    fun searchFor(phrase: String) {
        searchState.update { it.copy(phrase = phrase) }
    }

    fun setActive() {
        searchState.update { it.copy(isActive = true) }
    }

    fun disable() {
        searchState.update { it.copy(isActive = false) }
    }

    data class Search(
        val isActive: Boolean = false,
        val phrase: String = "",
    )
}