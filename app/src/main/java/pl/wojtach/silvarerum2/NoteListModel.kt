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

    fun reorder(draggedNoteIndex: Int, shouldBeBeforeIndex: Int) {
        Log.d("lw", "dragged note: $draggedNoteIndex reordered before index: $shouldBeBeforeIndex")
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