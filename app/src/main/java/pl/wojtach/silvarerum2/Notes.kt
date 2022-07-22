package pl.wojtach.silvarerum2

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
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

    private val _state: MutableState<ViewState> = mutableStateOf(ViewState(note, undoEnabled))
    val state: State<ViewState> get() = _state

    private val dbUpdates = Channel<NoteSnapshot>(CONFLATED).apply {
        consumeAsFlow()
            .onEach { update -> notesDao.update(update.toRoomEntity()) }
            .launchIn(this@EditNoteModel)
    }

    fun edit(newContent: String) {
        val prevEdit = state.value.note
        val newEdit = prevEdit.copy(content = newContent, lastModified = Timestamp(System.currentTimeMillis()))
        previousEdits.addFirst(prevEdit.content)
        _state.value = ViewState(newEdit, undoEnabled)
        dbUpdates.trySend(newEdit)
    }

    fun undo() {
        previousEdits.removeFirstOrNull()?.let { prevEdit ->
            val currentSnapshot = state.value.note
            val afterUndo = currentSnapshot.copy(content = prevEdit)
            _state.value = ViewState(afterUndo, undoEnabled)
            dbUpdates.trySend(afterUndo)
        }
    }

    data class ViewState(val note: NoteSnapshot, val undoEnabled: Boolean)
}