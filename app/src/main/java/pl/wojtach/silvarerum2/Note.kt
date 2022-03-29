package pl.wojtach.silvarerum2

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import pl.allegro.android.buyers.common.util.events.EventQueue
import java.util.UUID

class Notes() {

    private val state = MutableStateFlow(emptyList<NoteSnapshot>())

    val all: StateFlow<List<NoteSnapshot>>
        get() = state

    private val eventQueue = EventQueue<>

    fun add(newContent: String = "") {
        val timestamp = Timestamp(System.currentTimeMillis())
        val newNote = NoteSnapshot(NoteId(UUID.randomUUID().toString()), timestamp, newContent)
        state.update { notes -> notes + newNote }
    }

    fun get(id: NoteId): Flow<NoteSnapshot?> = all.map { notes -> notes.firstOrNull { it.id == id } }

    fun update(note: NoteSnapshot, newContent: String) {
        state.update { notes ->
            notes.mapMatching(note.id) { oldNote -> oldNote.copy(content = newContent) }
        }
    }

    fun delete(note: NoteSnapshot) {
        state.update { notes ->
            notes.filter { it.id != note.id }
        }
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
    class NoteClicked(noteId: NoteId): NavigationEvent()
    object AddNoteClicked : NavigationEvent()
    class EditNoteClicked
}
