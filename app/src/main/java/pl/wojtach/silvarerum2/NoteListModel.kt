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
        val newNoteReversedIndex = (state.value.firstOrNull()?.reversedShowIndex ?: -1) + 1
        val newNote =
            NoteSnapshot(NoteId(UUID.randomUUID().toString()), created = timestamp, "", reversedShowIndex = newNoteReversedIndex)
        launch {
            notesDao.insert(newNote.toRoomEntity())
        }
        return newNote
    }

    fun reorder(fromPosition: Int, toPosition: Int) {
        Log.d("lw", "dragged note from position: $fromPosition reordered to position: $toPosition")
        val items = state.value
        val from = items[fromPosition]
        val to = items[toPosition]
        val reorderedItem = from.copy(reversedShowIndex = to.reversedShowIndex)
        val isReorderUp = fromPosition > toPosition
        val switchedItems = if (isReorderUp) {
            items.subList(toPosition, fromPosition)
                .map { toBeSwitchedDown -> toBeSwitchedDown.copy(reversedShowIndex = toBeSwitchedDown.reversedShowIndex - 1) }
        } else {
            items.subList(fromPosition + 1, toPosition + 1)
                .map { toBeSwitchedUp -> toBeSwitchedUp.copy(reversedShowIndex = toBeSwitchedUp.reversedShowIndex + 1) }
        }
        launch {
            (switchedItems + reorderedItem)
                .map { updatedNote -> updatedNote.toRoomEntity() }
                .let { updatedNotes -> notesDao.updateAll(updatedNotes) }
        }
    }

    private class NoteListComparator : Comparator<NoteSnapshot> {

        override fun compare(first: NoteSnapshot, second: NoteSnapshot): Int {
            val byIndex = (first.reversedShowIndex compareTo second.reversedShowIndex) * -1
            val byLastModification = (first.lastModified.value compareTo second.lastModified.value) * -1
            return when {
                byIndex != 0            -> byIndex
                byLastModification != 0 -> byLastModification
                else                    -> {
                    (first.created.value compareTo second.created.value) * -1
                }
            }
        }
    }
}