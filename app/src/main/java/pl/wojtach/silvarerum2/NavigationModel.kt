package pl.wojtach.silvarerum2

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class NavigationModel {

    private val _state = MutableStateFlow<Destination>(Destination.NoteList)
    val state: StateFlow<Destination> get() = _state

    private val prevDestinations = ArrayDeque<Destination>(5)

    fun goTo(destination: Destination) {
        prevDestinations.addFirst(state.value)
        _state.tryEmit(destination)
    }

    fun popBackstack(upTo: String? = null): Boolean = when {
        prevDestinations.isEmpty() -> false
        upTo == null               -> {
            _state.tryEmit(prevDestinations.removeFirst())
            true
        }
        else                       -> popUpTo(upTo)
    }

    private tailrec fun popUpTo(name: String): Boolean =
        if (prevDestinations.isEmpty()) false
        else {
            val next = prevDestinations.removeFirst()
            if (next.name == name) {
                _state.tryEmit(next)
                true
            } else popUpTo(name)
        }
}

sealed interface Destination {
    val name: String

    object NoteList : Destination {
        override val name: String = "NoteList"
    }

    class ReadNote(val note: NoteSnapshot) : Destination {
        override val name: String = "ReadNote"
    }

    class EditNote(val note: NoteSnapshot) : Destination {
        override val name: String = "EditNote"
    }
}