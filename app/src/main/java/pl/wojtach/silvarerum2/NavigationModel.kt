package pl.wojtach.silvarerum2

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import pl.wojtach.silvarerum2.notelist.SearchableListModel
import pl.wojtach.silvarerum2.notelist.SearchableListModel.Search

class NavigationModel(
    initialDestination: Destination<Search> = Destination.NoteList(Search()),
    backstack: List<Destination<Any>> = emptyList()
) {

    private val _state = MutableStateFlow(initialDestination)
    val state: StateFlow<Destination<Any>> get() = _state

    private val prevDestinations = ArrayDeque<Destination<Any>>(5).apply {
        for (element in backstack) this.addLast(element)
    }

    val backStack: List<Destination<Any>> get() = prevDestinations.toList()

    fun <T> goTo(destination: Destination<T>) {
        prevDestinations.addFirst(state.value)
        _state.tryEmit(destination)
    }

    tailrec fun popBackstack(upTo: String? = null): Boolean =
        if (prevDestinations.isEmpty()) false
        else {
            val next = prevDestinations.removeFirst()
            if (upTo == null || next.name == upTo) {
                _state.tryEmit(next)
                true
            } else popBackstack(upTo)
        }
}

sealed interface Destination<T> {
    val name: String

    val dataModel: T

    class NoteList(search: Search) : Destination<Search> {
        override val name: String = "NoteList"
        override val dataModel: Search = search
    }

    class ReadNote(val note: NoteSnapshot) : Destination<Nothing> {
        override val name: String get() = destName
        override val dataModel: Nothing = TODO()

        companion object {
            const val destName: String = "ReadNote"
        }
    }

    class EditNote(val note: NoteSnapshot) : Destination<Nothing> {
        override val name: String get() = destName
        override val dataModel: Nothing = TODO()

        companion object {
            const val destName: String = "EditNote"
        }
    }
}