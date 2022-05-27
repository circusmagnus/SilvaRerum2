package pl.wojtach.silvarerum2

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.allegro.android.buyers.common.util.events.EventQueue
import pl.allegro.android.buyers.common.util.events.SharedEvents
import pl.wojtach.silvarerum2.utils.HasStableId
import pl.wojtach.silvarerum2.utils.updateSelected
import java.util.UUID

class Notes(scope: CoroutineScope): CoroutineScope by scope {

    private val state = MutableStateFlow(listOf(NoteSnapshot(noteId = NoteId("a"), Timestamp(System.currentTimeMillis()), content = "Ala ma kota")))

    val all: StateFlow<List<NoteSnapshot>>
        get() = state

    private val eventQueue = EventQueue<NavDestination>(64)
    val events: SharedEvents<NavDestination>
        get() = eventQueue

    fun add(newContent: String = "") {
        val timestamp = Timestamp(System.currentTimeMillis())
        val newNote = NoteSnapshot(NoteId(UUID.randomUUID().toString()), timestamp, newContent)
        state.update { notes -> notes + newNote }
        eventQueue.tryPush(NavDestination.EditNote(newNote.noteId))
    }

    fun get(id: NoteId): Flow<NoteSnapshot?> = all.map { notes -> notes.firstOrNull { it.noteId == id } }

    fun update(note: NoteSnapshot, newContent: String) {
        state.update { notes ->
            notes.updateSelected(note.noteId.value) { oldNote -> oldNote.copy(content = newContent) }
        }
    }

    fun edit(note: NoteSnapshot) {
        eventQueue.tryPush(NavDestination.EditNote(note.noteId))
    }

    fun delete(note: NoteSnapshot) {
        state.update { notes ->
            notes.filter { it.noteId != note.noteId }
        }
    }

    fun noteClicked(id: NoteId) {
        eventQueue.tryPush(NavDestination.ReadNote(id))
    }
}

data class NoteSnapshot(
    val noteId: NoteId,
    val created: Timestamp,
    val content: String
): HasStableId {
    override val id: String
        get() = noteId.value
}

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
