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

    private val addQueue = ClicksConsumer<String>(timeoutMs = 500) { addInput ->
        val timestamp = Timestamp(System.currentTimeMillis())
        val newNote = NoteSnapshot(NoteId(UUID.randomUUID().toString()), timestamp, addInput)
        eventQueue.tryPush(NavDestination.EditNote(newNote.noteId))
        state.update { notes -> notes + newNote }
    }

    fun <T> CoroutineScope.ClicksConsumer(timeoutMs: Long, consumeClick: (T) -> Unit, ): SendChannel<T> =
        Channel<T>(capacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST).apply {
        launch {
            consumeEach { content ->
                consumeClick(content)
                delay(timeoutMs)
                tryReceive() // throw away possible last value, which was sent during timeout
            }
        }
    }

    fun add(newContent: String = "") {
        addQueue.trySend(newContent)
    }

    fun get(id: NoteId): Flow<NoteSnapshot?> = all.map { notes -> notes.firstOrNull { it.noteId == id } }

    fun update(note: NoteSnapshot, newContent: String) {
        state.update { notes ->
            notes.updateSelected(note.noteId.value) { oldNote -> oldNote.copy(content = newContent) }
        }
    }

    private val editClicks = ClicksConsumer<NoteSnapshot>(timeoutMs = 500) { note ->
        eventQueue.tryPush(NavDestination.EditNote(note.noteId))
    }

    fun edit(note: NoteSnapshot) {
        editClicks.trySend(note)
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
