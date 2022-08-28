package pl.wojtach.silvarerum2.notelist

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import pl.wojtach.silvarerum2.NoteSnapshot

class SearchableListModel(private val baseModel: NoteListModel) : NoteListModel by baseModel {

    private val currentSearchPhrase = MutableStateFlow<String?>(null)
    private val applicableSearch = currentSearchPhrase
        .filter { it == null || (it.length in 1..2).not() }
        .map { phrase -> if (phrase?.length == 0) null else phrase }
        .debounce(500)

    override val state: StateFlow<List<NoteSnapshot>> = baseModel.state
        .combine(applicableSearch) { notes, searchPhrase ->
            if (searchPhrase == null) notes
            else notes.filter { note -> note.content.contains(searchPhrase) }
        }.stateIn(baseModel, started = SharingStarted.WhileSubscribed(), initialValue = emptyList())

    fun searchFor(phrase: String?) {
        currentSearchPhrase.value = phrase
    }
}