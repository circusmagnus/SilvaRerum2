package pl.wojtach.silvarerum2

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class NavigationModel(initialDestination: Destination = Destination.NoteList, backstack: List<Destination> = emptyList()) {

    private val _state = MutableStateFlow(initialDestination)
    val state: StateFlow<Destination> get() = _state

    private val prevDestinations = ArrayDeque<Destination>(5).apply {
        for (element in backstack) this.addLast(element)
    }

    val backStack: List<Destination> get() = prevDestinations.toList()

    fun goTo(destination: Destination) {
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