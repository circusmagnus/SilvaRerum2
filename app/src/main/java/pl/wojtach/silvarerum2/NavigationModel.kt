package pl.wojtach.silvarerum2

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class NavigationModel(initialDestination: Destination = Destination.NoteList, backstack: List<Destination> = emptyList()) {

    private val _state = MutableStateFlow<Destination>(initialDestination)
    val state: StateFlow<Destination> get() = _state

    val prevDestinations = ArrayDeque<Destination>(5).apply { addAll(backstack) }

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
        override val name: String get() = destName

        companion object {
            const val destName: String = "ReadNote"
        }
    }

    class EditNote(val note: NoteSnapshot) : Destination {
        override val name: String get() = destName

        companion object {
            const val destName: String = "EditNote"
        }
    }
}