package pl.wojtach.silvarerum2

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.CONFLATED
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import pl.wojtach.silvarerum2.room.NotesDao
import pl.wojtach.silvarerum2.room.toRoomEntity

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