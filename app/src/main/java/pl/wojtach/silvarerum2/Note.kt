package pl.wojtach.silvarerum2

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import pl.allegro.android.buyers.common.util.events.EventQueue
import pl.allegro.android.buyers.common.util.events.SharedEvents
import java.util.UUID

class Notes() {

    private val state = MutableStateFlow(listOf(NoteSnapshot(id = NoteId("a"), Timestamp(System.currentTimeMillis()), content = "Ala ma kota")))

    val all: StateFlow<List<NoteSnapshot>>
        get() = state

    private val eventQueue = EventQueue<NavDestination>(64)
    val events: SharedEvents<NavDestination>
        get() = eventQueue

    fun add(newContent: String = "") {
        val timestamp = Timestamp(System.currentTimeMillis())
        val newNote = NoteSnapshot(NoteId(UUID.randomUUID().toString()), timestamp, newContent)
        state.update { notes -> notes + newNote }
        eventQueue.tryPush(NavDestination.EditNote(newNote.id))
    }

    fun get(id: NoteId): Flow<NoteSnapshot?> = all.map { notes -> notes.firstOrNull { it.id == id } }

    fun update(note: NoteSnapshot, newContent: String) {
        state.update { notes ->
            notes.mapMatching(note.id) { oldNote -> oldNote.copy(content = newContent) }
        }
    }

    fun edit(note: NoteSnapshot) {
        eventQueue.tryPush(NavDestination.EditNote(note.id))
    }

    fun delete(note: NoteSnapshot) {
        state.update { notes ->
            notes.filter { it.id != note.id }
        }
    }

    fun noteClicked(id: NoteId) {
        eventQueue.tryPush(NavDestination.ReadNote(id))
    }
}

fun List<NoteSnapshot>.mapMatching(id: NoteId, transform: (NoteSnapshot) -> NoteSnapshot) = map { note ->
    if (note.id == id) transform(note) else note
}

data class NoteSnapshot(
    val id: NoteId,
    val created: Timestamp,
    val content: String
)

@JvmInline
value class NoteId(val value: String)

@JvmInline
value class Timestamp(val value: Long)

sealed class NavigationEvent {
    class NoteClicked(val noteId: NoteId) : NavigationEvent()
    object AddNoteClicked : NavigationEvent()
    class EditNoteClicked(val id: NoteId) : NavigationEvent()
    object ShowList : NavigationEvent()
}
