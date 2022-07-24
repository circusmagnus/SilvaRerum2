package pl.wojtach.silvarerum2

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pl.wojtach.silvarerum2.room.NotesDao
import pl.wojtach.silvarerum2.room.toRoomEntity

class ReadNoteModel(scope: CoroutineScope, private val note: NoteSnapshot, private val notesDao: NotesDao) : CoroutineScope by scope {

    val state: StateFlow<NoteSnapshot> = notesDao
        .getById(note.noteId.value)
        .map { it.asNote() }
        .stateIn(scope, SharingStarted.WhileSubscribed(), note)

    fun delete() {
        launch { notesDao.delete(note.toRoomEntity()) }
    }
}