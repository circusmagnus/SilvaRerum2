package pl.wojtach.silvarerum2

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
        .map { entities -> entities.map { it.asNote() }.sortedWith(NoteListComparator()) }
        .stateIn(scope, SharingStarted.WhileSubscribed(), emptyList())

    fun delete(note: NoteSnapshot) {
        launch {
            val items = state.value
            notesDao.delete(note.toRoomEntity())
            val indexReductionRange = items.subList(0, items.indexOf(note))
            indexReductionRange
                .map { note -> note.copy(reversedShowIndex = note.reversedShowIndex - 1).toRoomEntity() }
                .let { notesDao.updateAll(it) }
        }
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
        launch {
            val items = state.value
            val from = items[fromPosition]
            val to = items[toPosition]
            val reorderedItem = from.copy(reversedShowIndex = to.reversedShowIndex)
            val switchedAwayItem = to.copy(reversedShowIndex = from.reversedShowIndex)
            listOf(reorderedItem.toRoomEntity(), switchedAwayItem.toRoomEntity())
                .let { notesDao.updateAll(it) }
        }
    }

    private class NoteListComparator : Comparator<NoteSnapshot> {

        override fun compare(first: NoteSnapshot, second: NoteSnapshot): Int {
            val byIndex = (first.reversedShowIndex compareTo second.reversedShowIndex) * -1
            if (byIndex != 0) return byIndex

            val byLastModification = (first.lastModified.value compareTo second.lastModified.value) * -1
            return if (byLastModification != 0) byLastModification
            else (first.created.value compareTo second.created.value) * -1
        }
    }
}