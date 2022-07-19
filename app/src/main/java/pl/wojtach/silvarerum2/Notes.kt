package pl.wojtach.silvarerum2

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.CONFLATED
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
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
        .map { notes -> notes.sortedWith(LastModifiedComparator()) }
        .stateIn(scope, SharingStarted.WhileSubscribed(), emptyList())

    fun delete(note: NoteSnapshot) {
        launch { notesDao.delete(note.toRoomEntity()) }
    }

    fun addNew(): NoteSnapshot {
        val timestamp = Timestamp(System.currentTimeMillis())
        val newNote = NoteSnapshot(NoteId(UUID.randomUUID().toString()), created = timestamp, "",)
        launch {
            notesDao.insert(newNote.toRoomEntity())
        }
        return newNote
    }

    private class LastModifiedComparator: Comparator<NoteSnapshot> {

        override fun compare(first: NoteSnapshot, second: NoteSnapshot): Int {
            val byLastModification = (first.lastModified.value compareTo second.lastModified.value) * -1

            return if (byLastModification != 0) byLastModification else {
                (first.created.value compareTo second.created.value) * -1
            }
        }
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

    private val previousEdits = ArrayDeque<String>(20)
    private val undoEnabled get() = previousEdits.isNotEmpty()
    private val localNoteState = MutableStateFlow(note)

    val state: StateFlow<ViewState> = notesDao
        .getById(note.id)
        .map { it.asNote() }
        .combine(localNoteState) { dbNote, localNote -> pickLatest(dbNote, localNote) }
        .map { note -> ViewState(note, undoEnabled) }
        .stateIn(scope, SharingStarted.WhileSubscribed(), ViewState(note, undoEnabled))

    private val newEdits = Channel<String>(CONFLATED).apply {
        consumeAsFlow()
            .onEach { edit ->
                val prevEdit = state.value.note
                val newEdit = prevEdit.copy(content = edit, lastModified = Timestamp(System.currentTimeMillis()))
                localNoteState.emit(newEdit)
                notesDao.update(newEdit.toRoomEntity())
                previousEdits.addFirst(prevEdit.content)
            }.launchIn(this@EditNoteModel)
    }

    private val newUndos = Channel<Unit>(CONFLATED).apply {
        consumeAsFlow()
            .onEach {
                previousEdits.removeFirstOrNull()?.let { prevEdit ->
                    val currentSnapshot = state.value.note
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

    private fun pickLatest(note1: NoteSnapshot, note2: NoteSnapshot): NoteSnapshot =
        if(note1.created > note2.created)  note1 else note2

    data class ViewState(val note: NoteSnapshot, val undoEnabled: Boolean)
}