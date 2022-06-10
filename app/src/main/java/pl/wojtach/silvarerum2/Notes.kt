package pl.wojtach.silvarerum2

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.allegro.android.buyers.common.util.events.EventQueue
import pl.allegro.android.buyers.common.util.events.SharedEvents
import pl.wojtach.silvarerum2.room.NotesDao
import pl.wojtach.silvarerum2.room.toRoomEntity
import pl.wojtach.silvarerum2.utils.updateSelected
import java.util.UUID

class Notes(scope: CoroutineScope, private val notesDao: NotesDao): CoroutineScope by scope {

    private val state: MutableStateFlow<List<NoteSnapshot>> = MutableStateFlow<List<NoteSnapshot>>(emptyList()).apply {
        launch { emit(notesDao.getAll().first().map { it.asNote() }) }
    }

    val all: StateFlow<List<NoteSnapshot>>
        get() = state

    private val eventQueue = EventQueue<NavDestination>(64)
    val events: SharedEvents<NavDestination>
        get() = eventQueue

    init {
        state.drop(1).onEach { noteList -> noteList.forEach { notesDao.upsert(it.toRoomEntity()) } }.launchIn(this)
    }

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
        launch { notesDao.delete(note.toRoomEntity()) }
    }

    fun noteClicked(id: NoteId) {
        eventQueue.tryPush(NavDestination.ReadNote(id))
    }
}