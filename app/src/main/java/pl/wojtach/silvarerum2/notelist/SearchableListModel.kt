package pl.wojtach.silvarerum2.notelist

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import pl.wojtach.silvarerum2.NoteSnapshot

class SearchableListModel(private val baseModel: NoteListModel) : NoteListModel by baseModel {

    private val currentSearchState = MutableStateFlow(SearchState())
//    private val applicableSearch = currentSearchPhrase
//        .filter { it == null || (it.length in 1..2).not() }
//        .map { phrase -> if (phrase?.length == 0) null else phrase }

    val searchedState: StateFlow<NoteListState> = baseModel.state
        .combine(currentSearchState) { allNotes, searchState ->
            if (shouldFilter(searchState)) NoteListState(searchState, applySearch(allNotes, searchState))
            else NoteListState(searchState, allNotes)
        }.stateIn(baseModel, started = SharingStarted.WhileSubscribed(), initialValue = NoteListState())

    private suspend fun applySearch(notes: List<NoteSnapshot>, searchState: SearchState) = withContext(Dispatchers.Default) {
        notes.filter { note -> note.content.contains(searchState.phrase, ignoreCase = true) }
    }

    private fun shouldFilter(searchState: SearchState) =
        searchState.isActive && searchState.phrase.length > 2

    fun searchFor(phrase: String?) {
        currentSearchState.value = if (phrase != null) SearchState(true, phrase) else SearchState(false, "")
    }
}

data class NoteListState(
    val searchState: SearchState = SearchState(),
    val notes: List<NoteSnapshot> = emptyList()
)

data class SearchState(
    val isActive: Boolean = false,
    val phrase: String = "",
)