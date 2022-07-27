package pl.wojtach.silvarerum2

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pl.wojtach.silvarerum2.room.NotesDao
import pl.wojtach.silvarerum2.room.toRoomEntity
import java.util.UUID

class NoteListModel(scope: CoroutineScope, private val notesDao: NotesDao) : CoroutineScope by scope {

    val state: StateFlow<List<NoteSnapshot>> = notesDao
        .getAll()
        .map { entities -> entities.map { it.asNote() } }
        .map { notes -> notes.sortedWith(NoteListComparator()) }
        .stateIn(scope, SharingStarted.WhileSubscribed(), emptyList())

    fun delete(note: NoteSnapshot) {
        launch { notesDao.delete(note.toRoomEntity()) }
    }

    fun addNew(): NoteSnapshot {
        val timestamp = Timestamp(System.currentTimeMillis())
        val newNote = NoteSnapshot(NoteId(UUID.randomUUID().toString()), created = timestamp, "", priority = state.value.firstOrNull()?.priority ?: Int.MIN_VALUE)
        launch {
            notesDao.insert(newNote.toRoomEntity())
        }
        return newNote
    }

    fun reorder(fromIndex: Int, toIndex: Int) {
        Log.d("lw", "dragged note: $fromIndex reordered to index: $toIndex")
        val items = state.value
        val from = items[fromIndex]
        val to = items[toIndex]
//        val newPriority = to.priority + 1
        val updatedItem = from.copy(priority = to.priority + 1)
        val toBeSwichedUp = (items.subList(0, toIndex)  + updatedItem).map { it.copy(priority = it.priority + 1) }
        launch {
            notesDao.updateAll(toBeSwichedUp.map { it.toRoomEntity() })
        }
    }

    private class NoteListComparator : Comparator<NoteSnapshot> {

        override fun compare(first: NoteSnapshot, second: NoteSnapshot): Int {
            val byPriority = (first.priority compareTo second.priority) * -1
            val byLastModification = (first.lastModified.value compareTo second.lastModified.value) * -1
            return when {
                byPriority != 0         -> byPriority
                byLastModification != 0 -> byLastModification
                else                    -> {
                    (first.created.value compareTo second.created.value) * -1
                }
            }
        }
    }
}