package pl.wojtach.silvarerum2

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.CONFLATED
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pl.wojtach.silvarerum2.room.NotesDao
import pl.wojtach.silvarerum2.room.toRoomEntity
import java.util.UUID

class NoteListModel(scope: CoroutineScope, private val notesDao: NotesDao) : CoroutineScope by scope {

    val state: StateFlow<List<NoteSnapshot>> = notesDao
        .getAll()
        .map { entities -> entities.map { it.asNote() } }
        .stateIn(scope, SharingStarted.WhileSubscribed(), emptyList())

    fun delete(note: NoteSnapshot) {
        launch { notesDao.delete(note.toRoomEntity()) }
    }

    fun addNew(): NoteSnapshot {
        val timestamp = Timestamp(System.currentTimeMillis())
        val newNote = NoteSnapshot(NoteId(UUID.randomUUID().toString()), timestamp, "")
        launch {
            notesDao.insert(newNote.toRoomEntity())
        }
        return newNote
    }
}

class ReadNoteModel(scope: CoroutineScope, private val note: NoteSnapshot, private val notesDao: NotesDao) : CoroutineScope by scope {

    val state: StateFlow<NoteSnapshot> = notesDao
        .getById(note.noteId.value)
        .map { it.asNote() }
        .stateIn(scope, SharingStarted.WhileSubscribed(), note)

    fun delete() {
        launch { notesDao.delete(note.toRoomEntity()) }
    }
}

class EditNoteModel(scope: CoroutineScope, note: NoteSnapshot, private val notesDao: NotesDao) :
    CoroutineScope by scope {

    val state: StateFlow<NoteSnapshot> = notesDao
        .getById(note.id)
        .map { it.asNote() }
        .stateIn(scope, SharingStarted.WhileSubscribed(), note)

    private val previousEdits = ArrayDeque<String>(20)

    private val newEdits = Channel<String>(CONFLATED).apply {
        consumeAsFlow()
            .onEach { edit ->
                val prevEdit = state.value
                val newEdit = prevEdit.copy(content = edit)
                notesDao.update(newEdit.toRoomEntity())
                previousEdits.addFirst(prevEdit.content)
            }.launchIn(this@EditNoteModel)
    }

    private val newUndos = Channel<Unit>(CONFLATED).apply {
        consumeAsFlow()
            .onEach {
                previousEdits.removeFirstOrNull()?.let { prevEdit ->
                    val currentSnapshot = state.value
                    val afterUndo = currentSnapshot.copy(content = prevEdit)
                    notesDao.update(afterUndo.toRoomEntity())
                }
            }.launchIn(this@EditNoteModel)
    }

    fun edit(newContent: String) {
        newEdits.trySend(newContent)
    }

    fun undo() {
        newUndos.trySend(Unit)
    }
}

//class Notes(scope: CoroutineScope, private val notesDao: NotesDao): CoroutineScope by scope {
//
//    private val state: MutableStateFlow<List<NoteSnapshot>> = MutableStateFlow<List<NoteSnapshot>>(emptyList()).apply {
//        launch { emit(notesDao.getAll().first().map { it.asNote() }) }
//    }
//
//    val all: StateFlow<List<NoteSnapshot>>
//        get() = state
//
//    private val eventQueue = EventQueue<NavDestination>(64)
//    val events: SharedEvents<NavDestination>
//        get() = eventQueue
//
//    init {
//        state.drop(1).onEach { noteList -> noteList.forEach { notesDao.upsert(it.toRoomEntity()) } }.launchIn(this)
//    }
//
//    fun add(newContent: String = "") {
//        val timestamp = Timestamp(System.currentTimeMillis())
//        val newNote = NoteSnapshot(NoteId(UUID.randomUUID().toString()), timestamp, newContent)
//        state.update { notes -> notes + newNote }
//        eventQueue.tryPush(NavDestination.EditNote(newNote.noteId))
//    }
//
//    fun get(id: NoteId): Flow<NoteSnapshot?> = all.map { notes -> notes.firstOrNull { it.noteId == id } }
//
//    fun update(note: NoteSnapshot, newContent: String) {
//        state.update { notes ->
//            notes.updateSelected(note.noteId.value) { oldNote -> oldNote.copy(content = newContent) }
//        }
//    }
//
//    fun edit(note: NoteSnapshot) {
//        eventQueue.tryPush(NavDestination.EditNote(note.noteId))
//    }
//
//    fun delete(note: NoteSnapshot) {
//        state.update { notes ->
//            notes.filter { it.noteId != note.noteId }
//        }
//        launch { notesDao.delete(note.toRoomEntity()) }
//    }
//
//    fun noteClicked(id: NoteId) {
//        eventQueue.tryPush(NavDestination.ReadNote(id))
//    }
//}